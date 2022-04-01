#OkHttpDemo

### 为什么选择OkHttp？


* HTTP/2支持请求同一个host的多个请求共享一个socket连接。
* 连接池降低请求延迟（HTTP/2不可用的情况）。
* 透明的GZIP压缩下载体积。
* 响应缓存，避免完全重复的request发起网络请求，可以直接从缓存里面获取响应。
* 当OkHttp遇到网络问题的时候，它会静默的从常见的连接问题中恢复(RetryAndFollowUpInterceptor，重试重定向拦截器负责)。如果你的服务有多个IP地址，当第一次连接失败的时候，OkHttp会尝试其他的地址。对于IPv4+IPv6以及服务放在多个数据中心的情况，这是很重要的。OkHttp支持先进的TLS（传输层安全协议）特性。

* 使用OkHttp很简单。它的request/response API都是使用构建模式创建，并且是不可变的。OkHttp支持同步和异步请求。



[OkHttp 源码剖析系列（六）——连接复用机制及连接的建立](https://juejin.cn/post/6844904037167415310) 这个有时间再细看，现在先记住一些结论。

