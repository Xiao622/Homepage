/**
 * Mock 适配层：模拟真实接口的延迟，便于前端先行联调。
 * 对接 Spring Boot 后（VITE_USE_MOCK=false）不再走这里。
 */
export function mockDelay(ms = 200) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}
