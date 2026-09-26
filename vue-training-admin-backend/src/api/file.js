/**
 * 文件上传 - 接口地址
 * el-upload 的 action 直接用它, 走 Vite 的 /api 代理到后端 POST /files/upload
 * 后端返回的 url 形如 /api/uploads/2026/09/xxx.jpg (带 access-prefix, 适配 /api 代理)
 *
 * 注意: 后端要求扩展名白名单 + Content-Type 必须 image/* + 大小 <= 10MB
 */
export const uploadUrl = '/api/files/upload';

// 允许上传的图片类型(与后端白名单保持一致)
export const imageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/bmp', 'image/webp'];

// 单张图片大小上限 10MB
export const maxImageSize = 10 * 1024 * 1024;
