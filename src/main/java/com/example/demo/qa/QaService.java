package com.example.demo.qa;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.example.demo.result.ResultDTO;
import com.example.demo.story.AiText;
import com.example.demo.story.LifeEvidence;
import com.example.demo.story.SourceDTO;

@Service
public class QaService {

	public static final int MAX_QUESTION_LENGTH = 200;	// 질문 최대 글자 수
	public static final int MAX_QUESTIONS = 30;			// 인생 1개당 최대 질문 수 (AI 비용)
	private static final int RECENT_TURNS = 3;			// 이어지는 질문을 위해 같이 보낼 최근 대화 수

	private static final String SYSTEM_PROMPT = """
			너는 아시아 인생 시뮬레이션 게임 '환승인생'의 인생 Q&A 도우미야.
			[캐릭터 기록]은 게임 속 가상 인물의 일생이고, [역사·통계 자료]는 실제 자료야.
			규칙:
			1. 질문에 한국어로 2~5문장, 쉽고 친절하게 답해.
			2. 역사적 사실과 숫자는 [역사·통계 자료]에 있는 것만 쓰고, 그 문장 끝에 자료 번호를 [1]처럼 붙여.
			3. 자료에 없는 내용은 "준비된 자료에는 없어서 정확히 답하기 어려워요"라고 먼저 말한 뒤, 숫자 없이 일반적인 설명만 짧게 해.
			4. 이 인물의 개인사는 [캐릭터 기록]에 있는 것만 말하고, 지어내지 마. 게임 속 가상 인물이라는 점을 잊지 마.
			5. 이 인생이나 그 시대·나라와 관계없는 질문이면 "이 인생과 그 시대에 대한 질문에만 답할 수 있어요"라고 답해.
			6. 정치적 평가나 특정 집단 비하는 하지 마.
			""";

	private final QaDao qaDao;
	private final LifeEvidence evidence;	// C19와 같은 근거 글 (DB 조회 기반)
	private final ChatClient chatClient;

	public QaService(QaDao qaDao, LifeEvidence evidence, ChatClient.Builder chatClientBuilder) {
		this.qaDao = qaDao;
		this.evidence = evidence;
		this.chatClient = chatClientBuilder.build();
	}

	// 이 인생의 질문·답변 목록 (출처는 답변에서 인용한 것만)
	public List<QaDTO> list(int lifeId) {
		List<QaDTO> list = qaDao.selectQaList(lifeId);
		for (QaDTO qa : list) {
			qa.setSources(citedOnly(qa.getAnswer(), evidence.loadSources(qa.getSourceIds())));
		}
		return list;
	}

	public int count(int lifeId) {
		return qaDao.countQa(lifeId);
	}

	// ================= C20 인생 Q&A =================
	// 질문 → 근거 자료(DB) + 최근 대화와 함께 Gemini → 답변 저장 후 돌려줌 (실패 시 예외)
	public QaDTO ask(ResultDTO result, String question) {
		int lifeId = result.getLifeId();
		LifeEvidence.Facts facts = evidence.facts(lifeId);

		// 최근 대화 몇 개 (예: "그럼 그때 학교는?" 같은 이어지는 질문)
		StringBuilder recent = new StringBuilder();
		List<QaDTO> prev = qaDao.selectQaList(lifeId);
		for (int i = Math.max(0, prev.size() - RECENT_TURNS); i < prev.size(); i++) {
			recent.append("Q: ").append(prev.get(i).getQuestion()).append("\nA: ").append(prev.get(i).getAnswer()).append("\n");
		}

		String user = evidence.character(result) + "\n" + facts.text
				+ (recent.length() > 0 ? "\n[앞선 대화]\n" + recent : "")
				+ "\n[질문]\n" + question;

		String answer = AiText.join(chatClient.prompt()
				.system(SYSTEM_PROMPT)
				.user(user)
				.call()
				.chatResponse());
		if (answer == null || answer.isBlank()) {
			throw new IllegalStateException("AI가 빈 답변을 돌려줬어요.");
		}

		QaDTO qa = new QaDTO();
		qa.setLifeId(lifeId);
		qa.setQuestion(question);
		qa.setAnswer(answer.trim());
		qa.setSourceIds(evidence.joinIds(facts.sources));
		qaDao.insertQa(qa);

		QaDTO saved = qaDao.selectQa(qa.getQaId());	// 일시까지 다시 읽기
		saved.setSources(citedOnly(saved.getAnswer(), facts.sources));
		return saved;
	}

	// 답변 속 [번호]로 실제 인용한 출처만 남김
	private List<SourceDTO> citedOnly(String answer, List<SourceDTO> all) {
		List<SourceDTO> cited = new ArrayList<>();
		if (answer == null) {
			return cited;
		}
		Matcher m = Pattern.compile("\\[(\\d+)\\]").matcher(answer);
		List<Integer> nos = new ArrayList<>();
		while (m.find()) {
			nos.add(Integer.parseInt(m.group(1)));
		}
		for (SourceDTO s : all) {
			if (nos.contains(s.getNo())) {
				cited.add(s);
			}
		}
		return cited;
	}
}
