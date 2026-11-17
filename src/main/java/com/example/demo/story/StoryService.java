package com.example.demo.story;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.example.demo.result.EventLogDTO;
import com.example.demo.result.ResultDTO;

@Service
public class StoryService {

	private static final int MAX_HISTORY = 8;		// 근거로 넘길 역사 사건 최대 개수
	private static final int MAX_LOGS = 40;			// 근거로 넘길 인생 기록 최대 줄 수

	// AI에게 주는 규칙 (모든 이야기에 똑같이 적용)
	private static final String SYSTEM_PROMPT = """
			너는 아시아 인생 시뮬레이션 게임 '환승인생'의 인생 이야기 작가야.
			[캐릭터 기록]은 게임 속 가상 인물의 일생이고, [역사·통계 자료]는 실제 자료야.
			규칙:
			1. 가상 인물의 일생을 3인칭으로, 한국어 5~7문단(900~1300자)으로 써.
			2. 역사적 사실과 숫자는 [역사·통계 자료]에 있는 것만 써. 자료에 없는 사건·숫자·실존 인물은 만들지 마.
			3. 실제 자료를 근거로 쓴 문장 끝에는 자료 번호를 [1]처럼 붙여. 자료에 없는 번호는 쓰지 마.
			4. 캐릭터 기록에 없는 큰 일(결혼, 이민, 직업, 자녀 등)은 지어내지 마. 감정·풍경 묘사는 자유롭게 해도 돼.
			5. 차분한 회고 말투로 쓰고, 정치적 평가나 특정 집단 비하는 하지 마.
			6. 제목·머리말·맺음말 설명 없이 이야기 본문만 써.
			""";

	private final StoryDao storyDao;
	private final ChatClient chatClient;

	// ChatClient.Builder 는 spring-ai 가 application.properties 의 Gemini 설정으로 만들어 줌
	public StoryService(StoryDao storyDao, ChatClient.Builder chatClientBuilder) {
		this.storyDao = storyDao;
		this.chatClient = chatClientBuilder.build();
	}

	// ================= C19 AI 인생 이야기 =================
	// 저장된 이야기가 있으면 그대로, 없으면 Gemini로 새로 만들어 저장 (인생 1개당 1번만 호출)
	// result: C18 결과 (요약 + 인생 기록), Gemini 호출 실패 시 예외
	public StoryDTO getOrCreateStory(ResultDTO result) {
		int lifeId = result.getLifeId();

		// 1. 이미 만든 이야기
		StoryDTO saved = storyDao.selectStory(lifeId);
		if (saved != null) {
			saved.setSources(loadSources(saved.getSourceIds()));
			return saved;
		}

		// 2. 근거 자료 모으기 + 출처에 [1], [2] 번호 붙이기
		Map<Integer, SourceDTO> sourceMap = new LinkedHashMap<>();	// 출처 번호 → 출처 (넣은 순서 = 이야기 속 번호)
		String facts = buildFacts(lifeId, sourceMap);

		// 3. Gemini 호출
		String story = chatClient.prompt()
				.system(SYSTEM_PROMPT)
				.user(buildCharacter(result) + "\n" + facts)
				.call()
				.content();
		if (story == null || story.isBlank()) {
			throw new IllegalStateException("AI가 빈 이야기를 돌려줬어요.");
		}

		// 4. 저장
		StoryDTO dto = new StoryDTO();
		dto.setLifeId(lifeId);
		dto.setStory(story.trim());
		dto.setSourceIds(joinIds(sourceMap));
		try {
			storyDao.insertStory(dto);
		} catch (DuplicateKeyException e) {
			// 버튼을 두 번 눌러서 동시에 만든 경우: 먼저 저장된 것을 사용
			StoryDTO first = storyDao.selectStory(lifeId);
			first.setSources(loadSources(first.getSourceIds()));
			return first;
		}
		StoryDTO created = storyDao.selectStory(lifeId);	// 만든 날짜까지 다시 읽기
		created.setSources(new ArrayList<>(sourceMap.values()));
		return created;
	}

	// [캐릭터 기록]: 요약 + 인생 기록 (게임 속 가상 인물)
	private String buildCharacter(ResultDTO r) {
		StringBuilder sb = new StringBuilder("[캐릭터 기록]\n");
		sb.append("이름: ").append(r.getName()).append(" (").append("M".equals(r.getGender()) ? "남" : "여").append(")\n");
		sb.append("태어난 곳: ").append(r.getCountryName());
		if (r.getHistName() != null && !r.getHistName().equals(r.getCountryName())) {
			sb.append(" (당시 ").append(r.getHistName()).append(")");
		}
		sb.append(", ").append(r.getBirthYear()).append("년\n");
		if (r.getCurrentCountry() != null && !r.getCurrentCountry().equals(r.getBirthCountry())) {
			sb.append("마지막에 산 나라: ").append(r.getCurrentCountryName()).append("\n");
		}
		sb.append("사망: ").append(r.getDeathYear()).append("년, ").append(r.getDeathAge()).append("세, 원인 ").append(r.getDeathCause()).append("\n");
		sb.append("학력: ").append(nvl(r.getEduLevel())).append(" (").append(nvl(r.getSchoolStatus())).append(")\n");
		sb.append("마지막 직업: ").append(nvl(r.getJobName())).append("\n");
		sb.append("결혼: ").append("Y".equals(r.getMarriedYn()) ? "함" : "안 함").append(", 자녀 ").append(r.getChildCount()).append("명\n");
		sb.append("인생 기록:\n");
		List<EventLogDTO> events = r.getEvents();
		int n = Math.min(events.size(), MAX_LOGS);
		for (int i = 0; i < n; i++) {
			EventLogDTO e = events.get(i);
			sb.append("- ").append(e.getAge()).append("세(").append(e.getYearNo()).append("): ").append(e.getLogText());
			if (e.getChoiceText() != null) {
				sb.append(" → 선택: ").append(e.getChoiceText());
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	// [역사·통계 자료]: 겪은 역사 사건 우선 최대 8개 + 연대별 통계, 출처 번호 매기기
	private String buildFacts(int lifeId, Map<Integer, SourceDTO> sourceMap) {
		StringBuilder sb = new StringBuilder("[역사·통계 자료]\n");

		List<HistoryFactDTO> hist = storyDao.selectHistoryFacts(lifeId);
		int n = Math.min(hist.size(), MAX_HISTORY);
		for (int i = 0; i < n; i++) {
			HistoryFactDTO h = hist.get(i);
			sb.append("- ").append(h.getTitle()).append(" (").append(h.getStartYear()).append("~").append(h.getEndYear()).append(")");
			if ("Y".equals(h.getExperienced())) {
				sb.append(" ※ 이 인물이 직접 겪음");
			}
			sb.append(": ").append(nvl(h.getDescription())).append(cite(h.getSourceId(), h.getSourceTitle(), h.getSourceUrl(), sourceMap)).append("\n");
		}

		for (DecadeStatDTO s : storyDao.selectDecadeStats(lifeId)) {
			sb.append("- ").append(s.getDecade()).append("년대 이 나라: 기대수명 ").append(round(s.getLifeExpectancy())).append("세")
				.append(", 1인당 GDP ").append(round(s.getGdpPerCapita())).append("달러")
				.append(", 도시 인구 ").append(round(s.getUrbanRate())).append("%")
				.append(", 여성 1명당 출생아 ").append(s.getTfr()).append("명");
			if ("Y".equals(s.getIsEstimated())) {
				sb.append(" (추정값)");
			}
			sb.append(cite(s.getSourceId(), s.getSourceTitle(), s.getSourceUrl(), sourceMap)).append("\n");
		}
		return sb.toString();
	}

	// 출처에 번호를 붙여서 " [2]" 같은 글자를 돌려줌 (같은 출처는 같은 번호)
	private String cite(Integer sourceId, String title, String url, Map<Integer, SourceDTO> sourceMap) {
		if (sourceId == null) {
			return "";
		}
		if (!sourceMap.containsKey(sourceId)) {
			SourceDTO s = new SourceDTO();
			s.setSourceId(sourceId);
			s.setTitle(title);
			s.setUrl(url);
			sourceMap.put(sourceId, s);
		}
		int no = new ArrayList<>(sourceMap.keySet()).indexOf(sourceId) + 1;
		return " [" + no + "]";
	}

	// "3,7,12" → 출처 목록 (저장할 때의 순서 그대로)
	private List<SourceDTO> loadSources(String sourceIds) {
		List<SourceDTO> ordered = new ArrayList<>();
		if (sourceIds == null || sourceIds.isBlank()) {
			return ordered;
		}
		List<Integer> ids = new ArrayList<>();
		for (String s : sourceIds.split(",")) {
			ids.add(Integer.parseInt(s.trim()));
		}
		List<SourceDTO> found = storyDao.selectSources(ids);
		for (Integer id : ids) {
			for (SourceDTO s : found) {
				if (s.getSourceId() == id) {
					ordered.add(s);
				}
			}
		}
		return ordered;
	}

	private String joinIds(Map<Integer, SourceDTO> sourceMap) {
		List<String> ids = new ArrayList<>();
		for (Integer id : sourceMap.keySet()) {
			ids.add(String.valueOf(id));
		}
		return String.join(",", ids);
	}

	private String nvl(String s) {
		return s == null ? "없음" : s;
	}

	private String round(Double v) {
		return v == null ? "?" : String.valueOf(Math.round(v));
	}
}
