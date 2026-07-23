#!/bin/sh
# Claude Code PreToolUse 가드 — 네이티브 Grep 툴에 graphify 우선 사용을 안내한다.
#
# 왜 필요한가:
#   `graphify hook-guard`(0.9.12)는 tool_input 문자열에서 파일 확장자를 찾아낼 때만 발화한다.
#   Grep 툴의 입력은 {"pattern":"class EpisodeDao","path":"core"} 형태라 확장자가 없어
#   search·read 서브커맨드 어느 쪽도 반응하지 않는다(실측: 출력 0바이트).
#   glob 파라미터를 붙여도 마찬가지다. Grep은 그 자체가 코드 검색이므로
#   패턴 내용과 무관하게 항상 안내한다.
#
# 계약: stdout에 PreToolUse hookSpecificOutput JSON을 쓰고 항상 exit 0.
#       툴 호출을 차단하지 않는다(차단은 exit 2).

# 훅 페이로드를 소비한다. 읽지 않고 끝내면 호출 측이 broken pipe를 볼 수 있다.
cat >/dev/null 2>&1

# graphify 미설치 환경에서는 조용히 통과한다 — 기존 hook-guard 가드와 같은 정책.
command -v graphify >/dev/null 2>&1 || exit 0

# 그래프가 없으면 안내할 근거가 없다.
# 훅은 프로젝트 루트에서 실행되지만, 직접 실행될 때를 위해 스크립트 위치로도 폴백한다.
PROJECT_DIR="${CLAUDE_PROJECT_DIR:-$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)}"
[ -f "$PROJECT_DIR/graphify-out/graph.json" ] || exit 0

cat <<'JSON'
{"hookSpecificOutput":{"hookEventName":"PreToolUse","additionalContext":"MANDATORY: graphify-out/graph.json exists. Run `graphify query \"<question>\"` before Grep. Use `graphify explain \"<concept>\"` for a focused concept and `graphify path \"<A>\" \"<B>\"` for relationships. Only Grep after graphify has oriented you, or to modify/debug specific lines. This rule applies to subagents too — include it in every subagent prompt involving code exploration."}}
JSON
