# xmaster
第一次尝试提交项目到github。
</br>
做了一个集合了新闻、天气预报、音乐、翻译等等功能的APP。用了许多大神的开源库，和大厂的免费接口，总算把这些功能整合到了一个APP中。
</br>
</br>
在做这个APP之前，原本是想做一个视频播放器，用了b站的开源播放器ijkplayer，参考了许多github上的开源项目，如NiceVideoPlayer和dkplayer，然后自己模仿着也写成了一个module，可惜找不到免费的视频接口，只能播一下本地视频。后来找到了QQ音乐的接口，就在原来的基础上写了音乐播放器。之后又把以前写过的和风天气的天气预报、百度翻译和科大讯飞语音也整合到了一起。首页是实时新闻，包含了娱乐、体育、财经、军事等等新闻，用的是网易新闻的接口。
</br>
代码写得比较乱，没有什么参考价值，但毕竟做了这么久，功能也是实现了不少的，还是分享一下吧。
</br>
</br>
APP用了腾讯QMUI框架作为基本框架，BaseActivity继承了[SwipeBackFragment](https://github.com/YoKeyword/SwipeBackFragment)的SwipeBackActivity，fragment也可以滑动返回。下面是APP的一些展示。
</br>
初次加载界面图：
</br>
<img src="https://github.com/xuyiyiyi/xmaster/raw/master/screenshot/p1.jpg" width="150"/>
<img src="https://github.com/xuyiyiyi/xmaster/raw/master/screenshot/p2.jpg" width="150"/>
<img src="https://github.com/xuyiyiyi/xmaster/raw/master/screenshot/p3.jpg" width="150"/>
<img src="https://github.com/xuyiyiyi/xmaster/raw/master/screenshot/p4.jpg" width="150"/>
</br>
## 新闻
首页是网易新闻（平时不看新闻的我开始天天看新闻），点击新闻打开一个WebView，用的是腾讯的X5webview。
</br>
<img src="https://github.com/xuyiyiyi/xmaster/raw/master/screenshot/news.jpg" width="250"/>
<img src="https://github.com/xuyiyiyi/xmaster/raw/master/screenshot/webview.jpg" width="250"/>
</br>
## 音乐
音乐用了QQ音乐的音乐排行榜接口和搜索接口，歌词控件用的是[lrcview](https://github.com/wangchenyan/lrcview)。
</br>
<img src="https://github.com/xuyiyiyi/xmaster/raw/master/screenshot/music.jpg" width="200"/>
<img src="https://github.com/xuyiyiyi/xmaster/raw/master/screenshot/music1.jpg" width="200"/>
<img src="https://github.com/xuyiyiyi/xmaster/raw/master/screenshot/music2.jpg" width="200"/>
</br>
刚开始时觉得音乐播放器应该比视频播放器好做，毕竟只需要声音不需要画面的，直接把我原来播放器的TextureView去掉就好了。
</br>
做的时候才知道音乐播放器还要有歌词（直接用了开源代码），在前台播放（notification）、桌面小组件播放、锁屏界面播放，需要开启一个service来控制。
</br>
<img src="https://github.com/xuyiyiyi/xmaster/raw/master/screenshot/notification.jpg" width="250"/>
<img src="https://github.com/xuyiyiyi/xmaster/raw/master/screenshot/widget.jpg" width="250"/>
</br>
## 天气预报
天气预报用的和风天气的sdk，有实时天气、逐小时预报、七天预报、空气质量、城市搜索等等数据。
</br>
<img src="https://github.com/xuyiyiyi/xmaster/raw/master/screenshot/weather.jpg" width="250"/>
</br>
## 翻译
翻译是很久以前做的了，用的是百度翻译的接口，加入了科大讯飞语音输入和输出。
</br>
<img src="https://github.com/xuyiyiyi/xmaster/raw/master/screenshot/translate.jpg" width="250"/>
