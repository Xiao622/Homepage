/**
 * 音乐播放器 Mock 数据（VITE_USE_MOCK=true 时使用）。
 * 结构与后端 GET /api/music/tracks 返回一致：
 * { id, title, enTitle, artist, duration, coverSeed }
 * 复刻阶段无真实音频源：播放由 useMusicPlayer 模拟进度推进；
 * 接入真实音频后，把 url 换成真实音频地址即可无缝切换。
 */
export default [
  { id: 1, title: '漂游之人', enTitle: 'Drifting', artist: 'Leo Tapes', duration: 258, coverSeed: 0 },
  { id: 2, title: '金色之线', enTitle: 'Golden Thread', artist: 'Leo Tapes', duration: 245, coverSeed: 1 },
  { id: 3, title: '今夜星光灿烂', enTitle: 'E lucevan le stelle', artist: 'Leo Tapes', duration: 276, coverSeed: 2 },
  { id: 4, title: '长夜鸣笛', enTitle: 'The Midnight Whistle', artist: 'Leo Tapes', duration: 300, coverSeed: 3 },
  { id: 5, title: '雨中曲', enTitle: 'Dust to dust', artist: 'Leo Tapes', duration: 232, coverSeed: 4 },
  { id: 6, title: '相别', enTitle: 'Goodbye', artist: 'Leo Tapes', duration: 214, coverSeed: 5 },
  { id: 7, title: '雨后窗台', enTitle: 'Rainy Window', artist: 'Leo Tapes', duration: 198, coverSeed: 6 },
  { id: 8, title: '纸飞机', enTitle: 'Paper Planes', artist: 'Leo Tapes', duration: 187, coverSeed: 7 },
]
