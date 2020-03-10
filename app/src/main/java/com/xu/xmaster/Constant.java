package com.xu.xmaster;

import android.os.Environment;

public class Constant {

    //文件保存路径
    public static final String FileSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/xmaster/";

    //和风天气API
    public static final String weatherAPI = "https://free-api.heweather.com/s6/weather/";
    public static final String airAPI = "https://free-api.heweather.com/s6/air/";
    public static final String cityAPI = "https://search.heweather.net/find?";
    public static final String weather_type_now = "now";
    public static final String weather_type_forecast = "forecast";
    public static final String weather_type_hourly = "hourly";
    public static final String weather_key1 = "8ae6f0d768134091be43afd9f777ff5a";
    public static final String weather_key2 = "3bf3665b8f0940a1858d0a06b6ff64c3";
    public static final String weatherUsername = "HE1912261040551036";
    public static final String weatherKey = "8bacdc9934244ae19e3c34eea68bbe06";

    //百度翻译API
    public static final String translateAPI = "https://fanyi-api.baidu.com/api/trans/vip/translate";
    public static final String translate_appid = "20191118000358085";
    public static final String translate_key = "vSSThQ4mKoT8wy6q9hXd";

    //科大讯飞
    public static final String iFLYTEK_appid = "5bab3e6a";

    //网易新闻API
    //https://3g.163.com/touch/reconstruct/article/list/BBM54PGAwangning/0-10.html
    public static final String neteaseAPI = "https://3g.163.com/touch/reconstruct/article/list/";

    //QQ音乐API
    //搜索歌曲、歌手
    public static final String qqmusicSearchSong = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=1&n=20&format=json&w=";
    //搜索专辑
    public static final String qqmusicSearchAlbum = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=1&n=20&format=json&t=8&w=";
    //搜索专辑
    public static final String qqmusicSearchAlbumSong = "https://c.y.qq.com/v8/fcg-bin/fcg_v8_album_info_cp.fcg?albummid=";
    //获取歌曲key
    public static final String qqmusicKey = "https://u.y.qq.com/cgi-bin/musicu.fcg?format=json&data=%7B%22req_0%22%3A%7B%22module%22%3A%22vkey.GetVkeyServer%22%2C%22method%22%3A%22CgiGetVkey%22%2C%22param%22%3A%7B%22guid%22%3A%22358840384%22%2C%22songmid%22%3A%5B%22%songmid%22%5D%2C%22songtype%22%3A%5B0%5D%2C%22uin%22%3A%221443481947%22%2C%22loginflag%22%3A1%2C%22platform%22%3A%2220%22%7D%7D%2C%22comm%22%3A%7B%22uin%22%3A%2218585073516%22%2C%22format%22%3A%22json%22%2C%22ct%22%3A24%2C%22cv%22%3A0%7D%7D";
    //获取歌曲key
    public static String getQQmusicKey(String songmid) {
        return "https://u.y.qq.com/cgi-bin/musicu.fcg?format=json&data=" +
                "{\"req_0\":{" +
                "\"module\":\"vkey.GetVkeyServer\"," +
                "\"method\":\"CgiGetVkey\"," +
                "\"param\":{" +
                "\"guid\":\"1\"," +
                "\"songmid\":[\"" + songmid + "\"]," +
                "\"uin\":\"1\"" +
                "}}}";
    }
    //获取播放地址
    public static final String qqmusicUrl = "http://ws.stream.qqmusic.qq.com/";
    //获取歌词
    public static final String qqmusicLyric = "https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg?format=json&nobase64=1&songmid=";
    public static final String qqmusicHeadName = "Referer";
    public static final String qqmusicHeadValue = "https://y.qq.com/portal/player.html";

    public static int[] topid = {
            3, 4, 5, 6, 16, 17, 18, 19, 20, 21, 22, 23, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 64, 65, 66, 67, 70, 101, 102, 104, 105, 106, 107, 108, 113, 114, 117, 121, 122, 123, 124, 126, 127, 128, 129, 157, 158, 162, 167, 168, 169, 301, 427
    };

    public static int[] recommendTopid = {//推荐
            4, 26, 27, 427, 31, 34, 36, 60, 62, 64, 65, 158, 162, 167, 168
    };

    public static int[] topTopid = {//巅峰
            18, 19, 20, 21, 22, 23, 25, 28, 29, 30, 32, 33, 35, 50, 51, 52, 54, 55, 56
    };

    public static int[] regionTopid = {//地区
            3, 5, 6, 16, 17, 59, 61, 104, 105, 106, 107, 108, 113, 114, 117, 121, 122, 123, 126, 127, 128, 129
    };

    public static int[] otherTopid = {//其他
            53, 57, 58, 66, 67, 70, 101, 102, 124, 157, 169, 301
    };

    public static final String qqmusicAPI = "https://c.y.qq.com/v8/fcg-bin/fcg_v8_toplist_cp.fcg?g_tk=5381&uin=0&format=json&inCharset=utf-8&outCharset=utf-8¬ice=0&platform=h5&needNewCode=1&tpl=3&page=detail&type=top&topid=";

    public static String getQQmusicAlbumImg(String albummid) {
        return "https://y.gtimg.cn/music/photo_new/T002R300x300M000" + albummid + ".jpg";
    }
}
