package com.example.demo.story;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

// C19·C20 공통: Gemini 응답을 끝까지 이어 붙여서 글자로 만들기
// Gemini는 답을 여러 조각(part)으로 나눠 보낼 때가 있는데, .content()는 첫 조각만 돌려줘서 글이 중간에 끊김
public class AiText {

	public static String join(ChatResponse response) {
		if (response == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (Generation g : response.getResults()) {
			if (g.getOutput() != null && g.getOutput().getText() != null) {
				sb.append(g.getOutput().getText());
			}
			// 길이 제한 등으로 중간에 멈췄으면 콘솔에 이유 표시 (정상 종료는 STOP)
			String reason = g.getMetadata() == null ? null : g.getMetadata().getFinishReason();
			if (reason != null && !"STOP".equalsIgnoreCase(reason)) {
				System.out.println("[AI] 답변이 끝까지 오지 않았어요. finishReason=" + reason);
			}
		}
		return sb.toString();
	}
}
