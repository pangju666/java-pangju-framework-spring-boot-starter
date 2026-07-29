# CHANGELOG

## [2.0.1] 2026.3.26

- build (image): 增加tika-parser-image-module依赖
- perf (data-mongodb): 修正javadoc并优化代码
- perf (web-limit): 去除无用依赖
- perf (web-signature): 去除无用依赖并修正模块名称

## [2.1.0] 2026.6.7

- chore: 升级 spring-boot-starter-parent 为 4.1.0
- chore: 升级 Pangju Framework 为 2.1.0
- feat(image): 新增OpenCv操作模板实现类及相关配置
- feat(image): 新增GraphicsMagick的IOResource子类：GraphicsMagickResource
- feat(image): 新增GraphicsMagickUtils提供图像切片和图像信息解析方法
- feat(image): 新增图像切片配置TileOptions及其子类
- feat(image): 废弃旧的ImageOperation及其子类，新增ImageOperations及其子类
- feat(image): 废弃旧的ImageFile，改为使用IOResource及其子类
- feat(image): 新增ImageEngineException表示图像引擎错误，如：GraphicsMagick进程通讯错误
- feat(image): 新增Direction枚举表示方位、ImageCompressionType表示图像压缩类型
- perf(image): 废弃旧的ImageTemplate及其实现，新增ImageOperationsTemplate及其实现