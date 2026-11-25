// 커밋 메시지 형식: <type>: <요약> [#이슈번호]
// 예) feat: 회원가입 이메일 중복 검사 #12
module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      ['feat', 'fix', 'refactor', 'test', 'docs', 'style', 'chore', 'infra'],
    ],
    'subject-case': [0],
    'header-max-length': [2, 'always', 100],
  },
};
