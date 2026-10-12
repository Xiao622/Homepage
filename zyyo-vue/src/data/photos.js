/**
 * 相册（Photos）Mock 数据。
 * 真实相片属于站点作者版权内容，复刻中以 SVG 占位图展示结构；
 * 对接 Spring Boot 后，接口返回的 image 字段直接替换占位图。
 */
export const categories = [
  { key: 'daily', label: 'Daily', count: 74 },
  { key: 'streets', label: 'Streets', count: 74 },
  { key: 'nature', label: 'Nature', count: 68 },
  { key: 'food', label: 'Food', count: 68 },
]

/** 每类相册展示的占位条目数（真实数量见 categories.count） */
export const placeholderCount = 6

export const captions = {
  daily: ['晨跑的路上', '傍晚的阳台', '今天的咖啡', '窗外的云', '旧书摊', '雨后的街'],
  streets: ['巷口', '霓虹', '转角', '地铁站', '天桥', '夜行'],
  nature: ['桂花', '江风', '青灰的天', '落叶', '山影', '星夜'],
  food: ['热汤面', '周末早餐', '烤红薯', '水果摊', '深夜食堂', '手冲'],
}
