package io.jacob.episodive.core.testing.data

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.Medium
import io.jacob.episodive.core.model.Podcast
import kotlin.time.Instant

val podcastTestData =
    Podcast(
        id = 5778530,
        podcastGuid = "7eddd03e-20ae-5a29-ae59-a1a9c8091322",
        title = "슈카월드",
        url = "https://anchor.fm/s/ddaceaa8/podcast/rss",
        originalUrl = "http://pod.ssenhosting.com/rss/pb_33508/pbchannel_48285.xml",
        link = "https://podcasters.spotify.com/pod/show/qus1gd6ab6g",
        description = "어렵고 딱딱한 경제,시사,금융 이야기를\n쉽고 유쾌하게 풀어내는 \n경제/시사/이슈/잡썰 토크방송입니다.\n매일 오전 6시 슈카월드\n광고/ 제휴 문의 : syukaworld2@naver.com",
        author = "슈카친구들",
        ownerName = "슈카친구들",
        image = "https://d3t3ozftmdmh3i.cloudfront.net/production/podcast_uploaded_nologo/37090970/37090970-1679992813357-7ebb5a0de140c.jpg",
        artwork = "https://d3t3ozftmdmh3i.cloudfront.net/production/podcast_uploaded_nologo/37090970/37090970-1679992813357-7ebb5a0de140c.jpg",
        lastUpdateTime = Instant.fromEpochSeconds(1757568578),
        lastCrawlTime = Instant.fromEpochSeconds(1757568555),
        lastParseTime = Instant.fromEpochSeconds(1757568579),
        lastGoodHttpStatusTime = Instant.fromEpochSeconds(1757568555),
        lastHttpStatus = 200,
        contentType = "",
        itunesId = 1639494417,
        itunesType = null,
        generator = "Anchor Podcasts",
        language = "ko",
        explicit = false,
        type = 0,
        medium = Medium.Podcast,
        dead = 0,
        chash = null,
        episodeCount = 610,
        crawlErrors = 0,
        parseErrors = 0,
        categories = listOf(Category.BUSINESS),
        locked = 0,
        imageUrlHash = 1979648873,
        newestItemPubDate = Instant.fromEpochSeconds(1757451600),
    )
