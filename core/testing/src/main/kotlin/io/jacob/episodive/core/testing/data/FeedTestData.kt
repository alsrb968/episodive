package io.jacob.episodive.core.testing.data

import io.jacob.episodive.core.model.Category
import io.jacob.episodive.core.model.RecentFeed
import io.jacob.episodive.core.model.RecentNewFeed
import io.jacob.episodive.core.model.RecentNewValueFeed
import io.jacob.episodive.core.model.Soundbite
import io.jacob.episodive.core.model.TrendingFeed
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

val trendingFeedTestDataList = listOf(
    TrendingFeed(
        id = 1126846L,
        url = "https://feeds.megaphone.fm/ENTDM4274283917",
        title = "6 Rings and Football Things",
        description = "<p>Nick \"Fitzy\" Stevens leads a football-obsessed cast to bring you detailed coverage from inside the Patriots huddle. A fresh perspective on the games, storylines, and personalities on the field, in the front office, and around the NFL. Brought to you by WEEI and Audacy. New episodes drop weekly.</p>",
        author = "Audacy",
        image = "https://megaphone.imgix.net/podcasts/fe8574b6-f384-11ea-b2e9-8f07b796db2e/image/ce3becde7754cf3c10b863cb0f701ec4.png?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress",
        artwork = "https://megaphone.imgix.net/podcasts/fe8574b6-f384-11ea-b2e9-8f07b796db2e/image/ce3becde7754cf3c10b863cb0f701ec4.png?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044100L),
        itunesId = 1301767428L,
        trendScore = 9,
        language = "en",
        categories = listOf(Category.SPORTS)
    ),
    TrendingFeed(
        id = 6823582L,
        url = "https://www.spreaker.com/show/5842181/episodes/feed",
        title = "Rick Warren Sermons",
        description = "Richard Duane Warren is an American Baptist evangelical Christian pastor and author. He is the founder and senior pastor of Saddleback Church, an evangelical megachurch affiliated with the Southern Baptist Convention in Lake Forest, California.",
        author = "Word Life",
        image = "https://d3wo5wojvuv7l.cloudfront.net/images.spreaker.com/nuvolari-assets/yellow_square_mic.jpg",
        artwork = "https://d3wo5wojvuv7l.cloudfront.net/images.spreaker.com/nuvolari-assets/yellow_square_mic.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044107L),
        itunesId = null,
        trendScore = 9,
        language = "en",
        categories = listOf(Category.EDUCATION)
    ),
    TrendingFeed(
        id = 513944L,
        url = "http://www.noisemakerjoe.com/wtr?format=rss",
        title = "Writing The Rapids",
        description = "A writer curated discussion podcast and audio literary magazine",
        author = "Joe bielecki",
        image = "https://images.squarespace-cdn.com/content/v1/563f8821e4b04f0e6efac5ed/1518526522073-OFZUW5IL77S8K15X9AF8/WTR+ART.png?format=1500w",
        artwork = "https://images.squarespace-cdn.com/content/v1/563f8821e4b04f0e6efac5ed/1518526522073-OFZUW5IL77S8K15X9AF8/WTR+ART.png?format=1500w",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044122L),
        itunesId = 1348303650L,
        trendScore = 9,
        language = "en-US",
        categories = listOf(
            Category.ARTS,
            Category.VISUAL,
            Category.EDUCATION
        )
    ),
    TrendingFeed(
        id = 446880L,
        url = "https://wordonfire.podbean.com/feed.xml",
        title = "Bishop Barron's Sunday Sermons - Catholic Preaching and Homilies",
        description = "Weekly homilies from Bishop Robert Barron, produced by Word on Fire Catholic Ministries.",
        author = "Bishop Robert Barron",
        image = "https://pbcdn1.podbean.com/imglogo/image-logo/1224144/Bishop_Barrons_Sunday_Sermon_Podcast_2022_bssyd8.jpg",
        artwork = "https://pbcdn1.podbean.com/imglogo/image-logo/1224144/Bishop_Barrons_Sunday_Sermon_Podcast_2022_bssyd8.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044178L),
        itunesId = 75551187L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.RELIGION,
            Category.SPIRITUALITY
        )
    ),
    TrendingFeed(
        id = 6773932L,
        url = "https://feeds.acast.com/public/shows/67371180ba4404855a74dd08",
        title = "Hamsafar | همسفر",
        description = "<p>برای یک هدف همسفر شدیم</p><br><p><strong>روشن کردن مسیرِ رُشــــد ، با گسترش آگاهی و جسارت</strong></p><br><p>.</p><p>کاری داشتید توی اینستاگرام پیام بدید </p><p>.</p><p>.</p><p>@Hamsafarpodcast</p><p>@Sajjaddelangiiz</p><hr><p style='color:grey; font-size:0.75em;'> Hosted on Acast. See <a style='color:grey;' target='_blank' rel='noopener noreferrer' href='https://acast.com/privacy'>acast.com/privacy</a> for more information.</p>",
        author = "سجاد دل انگیز",
        image = "https://assets.pippa.io/shows/67371180ba4404855a74dd08/show-cover.jpg",
        artwork = "https://assets.pippa.io/shows/67371180ba4404855a74dd08/show-cover.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044211L),
        itunesId = 1697671815L,
        trendScore = 9,
        language = "fa",
        categories = listOf(
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT
        )
    ),
    TrendingFeed(
        id = 5977250L,
        url = "https://media.rss.com/thevanquishpodcast/feed.xml",
        title = "The Vancrux Podcast - Host Jeevan Matharu",
        description = "<p><strong>The Vancrux Podcast</strong> blends self-improvement with deep, philosophical inquiry to help you become your best self. We feature guests from all walks of life sharing transformative insights, along with solo episodes that explore mindset, meaning, and the pivotal choices we all face.</p><p><strong>Vancrux</strong> is a coined word — \"Van\" suggests rising or going beyond, and \"Crux\" means a turning point or critical moment. It reflects our belief that real growth begins where challenge meets choice. To Vancrux then, is to Vanquish at the crucial time. </p><p>We explore topics from Psychology and Philosophy to History, Science, and Spirituality — unfiltered, bold, and grounded in freedom of thought. This isn't just a podcast. It's a space for clarity, transformation, and purpose.</p><p><strong>Welcome to The Vancrux Podcast.</strong> Your turning point starts here.</p><p>Formerly known as the Vanquish Podcast.</p>",
        author = "Jeevan Matharu",
        image = "https://media.rss.com/thevanquishpodcast/20250714_080730_80573c4da70e1af2c577a8b1e5dee98c.png",
        artwork = "https://media.rss.com/thevanquishpodcast/20250714_080730_80573c4da70e1af2c577a8b1e5dee98c.png",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044392L),
        itunesId = 1680721489L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT,
            Category.SOCIETY,
            Category.CULTURE,
            Category.PHILOSOPHY
        )
    ),
    TrendingFeed(
        id = 705981L,
        url = "https://feeds.simplecast.com/MVTI95hg",
        title = "Locked On Giants – Daily Podcast On The San Francisco Giants",
        description = "Locked On Giants podcast is your daily ticket to stay ahead of the game and the first to know the latest news, analysis, and insider info for the San Francisco Giants and Major League Baseball. Hosted by Allen Stiles, a lifelong Giants fan, current radio host and digital creator that has covered the Giants for years, the Locked On Giants podcast provides your daily Giants fix with expert, local analysis, and coverage of all aspects of the Giants franchise, including game breakdowns and transactions in a data-driven way that's rational but simple, passionate and accessible. Locked On Giants takes you beyond the scoreboard for the inside scoops on the biggest stories from within the Giants locker room and all over MLB. The Locked On Giants podcast is part of the Locked On Podcast Network. Your Team. Every Day.",
        author = "Locked On Podcast Network, Ben Kaspick",
        image = "https://image.simplecastcdn.com/images/37b1400f-689a-40af-912a-977d765564aa/05c5f60b-5aa5-483d-8fac-a7a0b61fbf00/3000x3000/uploads-2f1551306418012-k5j0sitw2l-12101328be7269e7cdf1b920e4db4e6b-2flocked-on-giants-podcast-bg.jpg?aid=rss_feed",
        artwork = "https://image.simplecastcdn.com/images/37b1400f-689a-40af-912a-977d765564aa/05c5f60b-5aa5-483d-8fac-a7a0b61fbf00/3000x3000/uploads-2f1551306418012-k5j0sitw2l-12101328be7269e7cdf1b920e4db4e6b-2flocked-on-giants-podcast-bg.jpg?aid=rss_feed",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044400L),
        itunesId = 1455909225L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.SPORTS,
            Category.BASEBALL
        )
    ),
    TrendingFeed(
        id = 697653L,
        url = "https://anchor.fm/s/69742548/podcast/rss",
        title = "Podcast Agricultura",
        description = "Bienvenido a Podcast Agricultura, el primer podcast agrícola en español. Desde su lanzamiento, en enero de 2020, se ha consolidado como una fuente de información en el mundo agrícola.\nMi objetivo es ofrecer contenido de valor para profesionistas del sector agrícola. Ya seas un agrónomo experimentado o un recién graduado, aquí encontrarás una fuente de conocimiento.\nAbordo temas generales de agricultura, todos relacionados con la agroindustria y los agronegocios. Considero mi podcast como un complemento esencial para aquellos que han estudiado agronomía.",
        author = "Olmo Axayacatl",
        image = "https://d3t3ozftmdmh3i.cloudfront.net/production/podcast_uploaded_nologo/17592194/17592194-1642107347406-0594429d3ccf3.jpg",
        artwork = "https://d3t3ozftmdmh3i.cloudfront.net/production/podcast_uploaded_nologo/17592194/17592194-1642107347406-0594429d3ccf3.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044428L),
        itunesId = 1502973118L,
        trendScore = 9,
        language = "es-mx",
        categories = listOf(
            Category.SCIENCE,
            Category.LIFE
        )
    ),
    TrendingFeed(
        id = 3459952L,
        url = "https://www.spreaker.com/show/6431994/episodes/feed",
        title = "No Way, Jose!",
        description = "It's just me, Jose, talking to people I find interesting about the stuff that interests me. The stuff tends be parapolitics, political philosophy, and political commentary",
        author = "Jose Galison",
        image = "https://d3wo5wojvuv7l.cloudfront.net/t_rss_itunes_square_1400/images.spreaker.com/original/21fe78f8bfacdcd8ddd8466e26c0f9b6.jpg",
        artwork = "https://d3wo5wojvuv7l.cloudfront.net/t_rss_itunes_square_1400/images.spreaker.com/original/21fe78f8bfacdcd8ddd8466e26c0f9b6.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044442L),
        itunesId = 1546040443L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.NEWS,
            Category.POLITICS,
            Category.SOCIETY,
            Category.CULTURE,
            Category.PHILOSOPHY
        )
    ),
    TrendingFeed(
        id = 7394121L,
        url = "https://feeds.transistor.fm/ai-kidsnewsflash",
        title = "AI KidsNewsFlash",
        description = "AI KidsNewsFlash delivers the day's coolest artificial intelligence headlines in a fun, kid-friendly format—always in five minutes or less.\n\n🎧 Explore More Podcasts Built for Young Minds\n\n🧪 Science KidsNewsFlash\nDaily science and technology headlines, made fun and easy to understand.\n👉 sciencekidsnewsflash.com\n\n📰 KidsNewsFlash\nTop world news and current events, explained simply and made for kids.\n👉 kidsnewsflash.com\n\n🦁 Animal Battle Royale\nEpic creature vs. creature matchups packed with real science and wild fun.\n👉 animalbattleroyale.com",
        author = "KidsNewsFlash Universe",
        image = "https://img.transistor.fm/pL0kqTxqoZH7cD0Aw82cutVJuq4QAgjDS-oyT1WBQis/rs:fill:0:0:1/w:1400/h:1400/q:60/mb:500000/aHR0cHM6Ly9pbWct/dXBsb2FkLXByb2R1/Y3Rpb24udHJhbnNp/c3Rvci5mbS8wOGY2/ODcxNjQyYjYxNTEx/MDMzMTc4ZTE5OTlm/ZjBmZC5wbmc.jpg",
        artwork = "https://img.transistor.fm/pL0kqTxqoZH7cD0Aw82cutVJuq4QAgjDS-oyT1WBQis/rs:fill:0:0:1/w:1400/h:1400/q:60/mb:500000/aHR0cHM6Ly9pbWct/dXBsb2FkLXByb2R1/Y3Rpb24udHJhbnNp/c3Rvci5mbS8wOGY2/ODcxNjQyYjYxNTEx/MDMzMTc4ZTE5OTlm/ZjBmZC5wbmc.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044454L),
        itunesId = 1823549203L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.NEWS,
            Category.DAILY,
            Category.KIDS,
            Category.FAMILY,
            Category.EDUCATION
        )
    )
)

val trendingFeedTestData = trendingFeedTestDataList.first()

val recentFeedTestDataList = listOf(
    RecentFeed(
        id = 1126846L,
        url = "https://feeds.megaphone.fm/ENTDM4274283917",
        title = "6 Rings and Football Things",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044100L),
        oldestItemPublishTime = null,
        description = "<p>Nick \"Fitzy\" Stevens leads a football-obsessed cast to bring you detailed coverage from inside the Patriots huddle. A fresh perspective on the games, storylines, and personalities on the field, in the front office, and around the NFL. Brought to you by WEEI and Audacy. New episodes drop weekly.</p>",
        author = "Audacy",
        image = "https://megaphone.imgix.net/podcasts/fe8574b6-f384-11ea-b2e9-8f07b796db2e/image/ce3becde7754cf3c10b863cb0f701ec4.png?ixlib=rails-4.3.1&max-w=3000&max-h=3000&fit=crop&auto=format,compress",
        itunesId = 1301767428L,
        trendScore = 9,
        language = "en",
        categories = listOf(Category.SPORTS)
    ),
    RecentFeed(
        id = 6823582L,
        url = "https://www.spreaker.com/show/5842181/episodes/feed",
        title = "Rick Warren Sermons",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044107L),
        oldestItemPublishTime = null,
        description = "Richard Duane Warren is an American Baptist evangelical Christian pastor and author. He is the founder and senior pastor of Saddleback Church, an evangelical megachurch affiliated with the Southern Baptist Convention in Lake Forest, California.",
        author = "Word Life",
        image = "https://d3wo5wojvuv7l.cloudfront.net/images.spreaker.com/nuvolari-assets/yellow_square_mic.jpg",
        itunesId = null,
        trendScore = 9,
        language = "en",
        categories = listOf(Category.EDUCATION)
    ),
    RecentFeed(
        id = 513944L,
        url = "http://www.noisemakerjoe.com/wtr?format=rss",
        title = "Writing The Rapids",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044122L),
        oldestItemPublishTime = null,
        description = "A writer curated discussion podcast and audio literary magazine",
        author = "Joe bielecki",
        image = "https://images.squarespace-cdn.com/content/v1/563f8821e4b04f0e6efac5ed/1518526522073-OFZUW5IL77S8K15X9AF8/WTR+ART.png?format=1500w",
        itunesId = 1348303650L,
        trendScore = 9,
        language = "en-US",
        categories = listOf(
            Category.ARTS,
            Category.VISUAL,
            Category.EDUCATION
        )
    ),
    RecentFeed(
        id = 446880L,
        url = "https://wordonfire.podbean.com/feed.xml",
        title = "Bishop Barron's Sunday Sermons - Catholic Preaching and Homilies",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044178L),
        oldestItemPublishTime = null,
        description = "Weekly homilies from Bishop Robert Barron, produced by Word on Fire Catholic Ministries.",
        author = "Bishop Robert Barron",
        image = "https://pbcdn1.podbean.com/imglogo/image-logo/1224144/Bishop_Barrons_Sunday_Sermon_Podcast_2022_bssyd8.jpg",
        itunesId = 75551187L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.RELIGION,
            Category.SPIRITUALITY
        )
    ),
    RecentFeed(
        id = 6773932L,
        url = "https://feeds.acast.com/public/shows/67371180ba4404855a74dd08",
        title = "Hamsafar | همسفر",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044211L),
        oldestItemPublishTime = null,
        description = "<p>برای یک هدف همسفر شدیم</p><br><p><strong>روشن کردن مسیرِ رُشــــد ، با گسترش آگاهی و جسارت</strong></p><br><p>.</p><p>کاری داشتید توی اینستاگرام پیام بدید </p><p>.</p><p>.</p><p>@Hamsafarpodcast</p><p>@Sajjaddelangiiz</p><hr><p style='color:grey; font-size:0.75em;'> Hosted on Acast. See <a style='color:grey;' target='_blank' rel='noopener noreferrer' href='https://acast.com/privacy'>acast.com/privacy</a> for more information.</p>",
        author = "سجاد دل انگیز",
        image = "https://assets.pippa.io/shows/67371180ba4404855a74dd08/show-cover.jpg",
        itunesId = 1697671815L,
        trendScore = 9,
        language = "fa",
        categories = listOf(
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT
        )
    ),
    RecentFeed(
        id = 5977250L,
        url = "https://media.rss.com/thevanquishpodcast/feed.xml",
        title = "The Vancrux Podcast - Host Jeevan Matharu",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044392L),
        oldestItemPublishTime = null,
        description = "<p><strong>The Vancrux Podcast</strong> blends self-improvement with deep, philosophical inquiry to help you become your best self. We feature guests from all walks of life sharing transformative insights, along with solo episodes that explore mindset, meaning, and the pivotal choices we all face.</p><p><strong>Vancrux</strong> is a coined word — \"Van\" suggests rising or going beyond, and \"Crux\" means a turning point or critical moment. It reflects our belief that real growth begins where challenge meets choice. To Vancrux then, is to Vanquish at the crucial time. </p><p>We explore topics from Psychology and Philosophy to History, Science, and Spirituality — unfiltered, bold, and grounded in freedom of thought. This isn't just a podcast. It's a space for clarity, transformation, and purpose.</p><p><strong>Welcome to The Vancrux Podcast.</strong> Your turning point starts here.</p><p>Formerly known as the Vanquish Podcast.</p>",
        author = "Jeevan Matharu",
        image = "https://media.rss.com/thevanquishpodcast/20250714_080730_80573c4da70e1af2c577a8b1e5dee98c.png",
        itunesId = 1680721489L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT,
            Category.SOCIETY,
            Category.CULTURE,
            Category.PHILOSOPHY
        )
    ),
    RecentFeed(
        id = 705981L,
        url = "https://feeds.simplecast.com/MVTI95hg",
        title = "Locked On Giants – Daily Podcast On The San Francisco Giants",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044400L),
        oldestItemPublishTime = null,
        description = "Locked On Giants podcast is your daily ticket to stay ahead of the game and the first to know the latest news, analysis, and insider info for the San Francisco Giants and Major League Baseball. Hosted by Allen Stiles, a lifelong Giants fan, current radio host and digital creator that has covered the Giants for years, the Locked On Giants podcast provides your daily Giants fix with expert, local analysis, and coverage of all aspects of the Giants franchise, including game breakdowns and transactions in a data-driven way that's rational but simple, passionate and accessible. Locked On Giants takes you beyond the scoreboard for the inside scoops on the biggest stories from within the Giants locker room and all over MLB. The Locked On Giants podcast is part of the Locked On Podcast Network. Your Team. Every Day.",
        author = "Locked On Podcast Network, Ben Kaspick",
        image = "https://image.simplecastcdn.com/images/37b1400f-689a-40af-912a-977d765564aa/05c5f60b-5aa5-483d-8fac-a7a0b61fbf00/3000x3000/uploads-2f1551306418012-k5j0sitw2l-12101328be7269e7cdf1b920e4db4e6b-2flocked-on-giants-podcast-bg.jpg?aid=rss_feed",
        itunesId = 1455909225L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.SPORTS,
            Category.BASEBALL
        )
    ),
    RecentFeed(
        id = 697653L,
        url = "https://anchor.fm/s/69742548/podcast/rss",
        title = "Podcast Agricultura",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044428L),
        oldestItemPublishTime = null,
        description = "Bienvenido a Podcast Agricultura, el primer podcast agrícola en español. Desde su lanzamiento, en enero de 2020, se ha consolidado como una fuente de información en el mundo agrícola.\nMi objetivo es ofrecer contenido de valor para profesionistas del sector agrícola. Ya seas un agrónomo experimentado o un recién graduado, aquí encontrarás una fuente de conocimiento.\nAbordo temas generales de agricultura, todos relacionados con la agroindustria y los agronegocios. Considero mi podcast como un complemento esencial para aquellos que han estudiado agronomía.",
        author = "Olmo Axayacatl",
        image = "https://d3t3ozftmdmh3i.cloudfront.net/production/podcast_uploaded_nologo/17592194/17592194-1642107347406-0594429d3ccf3.jpg",
        itunesId = 1502973118L,
        trendScore = 9,
        language = "es-mx",
        categories = listOf(
            Category.SCIENCE,
            Category.LIFE
        )
    ),
    RecentFeed(
        id = 3459952L,
        url = "https://www.spreaker.com/show/6431994/episodes/feed",
        title = "No Way, Jose!",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044442L),
        oldestItemPublishTime = null,
        description = "It's just me, Jose, talking to people I find interesting about the stuff that interests me. The stuff tends be parapolitics, political philosophy, and political commentary",
        author = "Jose Galison",
        image = "https://d3wo5wojvuv7l.cloudfront.net/t_rss_itunes_square_1400/images.spreaker.com/original/21fe78f8bfacdcd8ddd8466e26c0f9b6.jpg",
        itunesId = 1546040443L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.NEWS,
            Category.POLITICS,
            Category.SOCIETY,
            Category.CULTURE,
            Category.PHILOSOPHY
        )
    ),
    RecentFeed(
        id = 7394121L,
        url = "https://feeds.transistor.fm/ai-kidsnewsflash",
        title = "AI KidsNewsFlash",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044454L),
        oldestItemPublishTime = null,
        description = "AI KidsNewsFlash delivers the day's coolest artificial intelligence headlines in a fun, kid-friendly format—always in five minutes or less.\n\n🎧 Explore More Podcasts Built for Young Minds\n\n🧪 Science KidsNewsFlash\nDaily science and technology headlines, made fun and easy to understand.\n👉 sciencekidsnewsflash.com\n\n📰 KidsNewsFlash\nTop world news and current events, explained simply and made for kids.\n👉 kidsnewsflash.com\n\n🦁 Animal Battle Royale\nEpic creature vs. creature matchups packed with real science and wild fun.\n👉 animalbattleroyale.com",
        author = "KidsNewsFlash Universe",
        image = "https://img.transistor.fm/pL0kqTxqoZH7cD0Aw82cutVJuq4QAgjDS-oyT1WBQis/rs:fill:0:0:1/w:1400/h:1400/q:60/mb:500000/aHR0cHM6Ly9pbWct/dXBsb2FkLXByb2R1/Y3Rpb24udHJhbnNp/c3Rvci5mbS8wOGY2/ODcxNjQyYjYxNTEx/MDMzMTc4ZTE5OTlm/ZjBmZC5wbmc.jpg",
        itunesId = 1823549203L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.NEWS,
            Category.DAILY,
            Category.KIDS,
            Category.FAMILY,
            Category.EDUCATION
        )
    )
)

val recentFeedTestData = recentFeedTestDataList.first()

val recentNewFeedTestDataList = listOf(
    RecentNewFeed(
        id = 7489312L,
        url = "https://www.spreaker.com/show/6740217/episodes/feed",
        timeAdded = Instant.fromEpochSeconds(1758086236L),
        status = "pending",
        contentHash = "",
        language = ""
    ),
    RecentNewFeed(
        id = 7489311L,
        url = "https://anchor.fm/s/108187b18/podcast/rss",
        timeAdded = Instant.fromEpochSeconds(1758086134L),
        status = "pending",
        contentHash = "",
        language = ""
    ),
    RecentNewFeed(
        id = 7489310L,
        url = "https://sadikul.globalitsolution.net/feed/podcast/podcast-website/",
        timeAdded = Instant.fromEpochSeconds(1758085883L),
        status = "confirmed",
        contentHash = "",
        language = "en-US"
    ),
    RecentNewFeed(
        id = 7489309L,
        url = "https://anchor.fm/s/108a0bd20/podcast/rss",
        timeAdded = Instant.fromEpochSeconds(1758085675L),
        status = "confirmed",
        contentHash = "",
        language = "pt"
    ),
    RecentNewFeed(
        id = 7489308L,
        url = "https://anchor.fm/s/105b8d994/podcast/rss",
        timeAdded = Instant.fromEpochSeconds(1758085672L),
        status = "confirmed",
        contentHash = "",
        language = "de-ch"
    ),
    RecentNewFeed(
        id = 7489307L,
        url = "https://app.springcast.fm/podcast-xml/22173",
        timeAdded = Instant.fromEpochSeconds(1758085651L),
        status = "confirmed",
        contentHash = "",
        language = "nl"
    ),
    RecentNewFeed(
        id = 7489306L,
        url = "https://juliefillinger.com/feed/podcast/julie-fillinger/",
        timeAdded = Instant.fromEpochSeconds(1758085589L),
        status = "confirmed",
        contentHash = "",
        language = "en"
    ),
    RecentNewFeed(
        id = 7489305L,
        url = "https://www.spreaker.com/show/6740215/episodes/feed",
        timeAdded = Instant.fromEpochSeconds(1758085525L),
        status = "confirmed",
        contentHash = "",
        language = "fa"
    ),
    RecentNewFeed(
        id = 7489304L,
        url = "https://feeds.transistor.fm/entendez-vous-la-terre",
        timeAdded = Instant.fromEpochSeconds(1758085472L),
        status = "confirmed",
        contentHash = "",
        language = "fr"
    ),
    RecentNewFeed(
        id = 7489303L,
        url = "https://anchor.fm/s/108e14a84/podcast/rss",
        timeAdded = Instant.fromEpochSeconds(1758085408L),
        status = "confirmed",
        contentHash = "",
        language = "zh-cn"
    )
)

val recentNewFeedTestData = recentNewFeedTestDataList.first()

val recentNewValueFeedTestDataList = listOf(
    RecentNewValueFeed(
        id = 7489194L,
        url = "https://regalaunjajaja.com/clon/feed/podcast/regala-una-sonrisa/",
        title = "Regala una Sonrisa",
        author = "Regala una Sonrisa",
        image = "https://regalaunjajaja.com/clon/wp-content/uploads/2018/10/libreta-regala-una-sonrisa-16-1.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1678160387L),
        itunesId = null,
        trendScore = 0,
        language = "es",
        categories = listOf(
            Category.HEALTH,
            Category.FITNESS,
            Category.MENTAL,
            Category.SOCIETY,
            Category.CULTURE,
            Category.EDUCATION
        )
    ),
    RecentNewValueFeed(
        id = 6677833L,
        url = "https://media.rss.com/redheadranting/feed.xml",
        title = "Redhead Ranting",
        author = "Redhead Ranting",
        image = "https://media.rss.com/redheadranting/20250916_100931_befe33bfc518e8badcfd4864719c7859.png",
        newestItemPublishTime = Instant.fromEpochSeconds(1758061308L),
        itunesId = 1716639126L,
        trendScore = 0,
        language = "en",
        categories = listOf(
            Category.SOCIETY,
            Category.CULTURE,
            Category.NEWS,
            Category.COMMENTARY
        )
    ),
    RecentNewValueFeed(
        id = 7487395L,
        url = "https://media.rss.com/dopamine-detox-by-thibaut-meurisse-book-summary-podcast-english/feed.xml",
        title = "Dopamine Detox by Thibaut Meurisse, Book Summary, Podcast, English",
        author = "Raghvendra Singh",
        image = "https://media.rss.com/dopamine-detox-by-thibaut-meurisse-book-summary-podcast-english/20250915_030930_786a9587e7ac9c126c539ac2ea8c8ad6.png",
        newestItemPublishTime = Instant.fromEpochSeconds(1757949685L),
        itunesId = 1839953302L,
        trendScore = 0,
        language = "en",
        categories = listOf(
            Category.ARTS,
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT
        )
    ),
    RecentNewValueFeed(
        id = 7488676L,
        url = "https://media.rss.com/first-lie-wins-by-ashley-elston-book-summary-podcast-english/feed.xml",
        title = "First Lie Wins by Ashley Elston, Book Summary, Podcast, English",
        author = "Raghvendra Singh",
        image = "https://media.rss.com/first-lie-wins-by-ashley-elston-book-summary-podcast-english/20250916_050938_97b2477f9d74ef8bb6541285a447eebb.png",
        newestItemPublishTime = Instant.fromEpochSeconds(1758044412L),
        itunesId = null,
        trendScore = 0,
        language = "en",
        categories = listOf(
            Category.ARTS,
            Category.COMEDY
        )
    ),
    RecentNewValueFeed(
        id = 7488653L,
        url = "https://media.rss.com/broken-country-by-clare-leslie-hall-book-summary-podcast-english/feed.xml",
        title = "Broken Country by Clare Leslie Hall, Book Summary, Podcast, English",
        author = "Raghvendra Singh",
        image = "https://media.rss.com/broken-country-by-clare-leslie-hall-book-summary-podcast-english/20250916_050920_3b6805d27e8d1ddc51a1e2ddf756ebdc.png",
        newestItemPublishTime = Instant.fromEpochSeconds(1758043066L),
        itunesId = null,
        trendScore = 0,
        language = "en",
        categories = listOf(
            Category.ARTS,
            Category.COMEDY
        )
    ),
    RecentNewValueFeed(
        id = 7488675L,
        url = "https://media.rss.com/you-are-a-badass-by-jen-sincero-book-summary-podcast-english/feed.xml",
        title = "You Are a Badass by Jen Sincero, Book Summary, Podcast, English",
        author = "Raghvendra Singh",
        image = "https://media.rss.com/you-are-a-badass-by-jen-sincero-book-summary-podcast-english/20250916_040904_fdd082b3016e302a8cde8abe81eef90a.png",
        newestItemPublishTime = Instant.fromEpochSeconds(1758041669L),
        itunesId = 1840218988L,
        trendScore = 0,
        language = "en",
        categories = listOf(
            Category.ARTS,
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT
        )
    ),
    RecentNewValueFeed(
        id = 7484154L,
        url = "https://media.rss.com/the-daily-stoic-by-ryan-holiday-book-summary-podcast-english/feed.xml",
        title = "The Daily Stoic by Ryan Holiday, Book Summary, Podcast, English",
        author = "Raghvendra Singh",
        image = "https://media.rss.com/the-daily-stoic-by-ryan-holiday-book-summary-podcast-english/20250911_050926_00c5861a9c0c20e12edbee3e3c466102.png",
        newestItemPublishTime = Instant.fromEpochSeconds(1757691698L),
        itunesId = 1839488306L,
        trendScore = 0,
        language = "en",
        categories = listOf(
            Category.ARTS,
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT
        )
    ),
    RecentNewValueFeed(
        id = 7482466L,
        url = "https://media.rss.com/debate-the-greats-podcast/feed.xml",
        title = "Debate The Greats Podcast",
        author = "Charlie B. James",
        image = "https://media.rss.com/debate-the-greats-podcast/20250911_040931_3983e710a8590bfb4c00eb705fd7ad89.png",
        newestItemPublishTime = Instant.fromEpochSeconds(1757571137L),
        itunesId = 1839150400L,
        trendScore = 0,
        language = "en",
        categories = listOf(
            Category.SPORTS
        )
    ),
    RecentNewValueFeed(
        id = 7487617L,
        url = "https://feeds.fountain.fm/JOopP2CpndCzDzbAeAy9",
        title = "The Jake Woodhouse Podcast",
        author = "Jake Woodhouse",
        image = "https://feeds.fountain.fm/JOopP2CpndCzDzbAeAy9/files/COVER_ART---DEFAULT---60ef7758-7d0c-42d8-991b-e812f0c96da6.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1757964410L),
        itunesId = null,
        trendScore = 0,
        language = "en",
        categories = listOf(
            Category.BUSINESS,
            Category.INVESTING
        )
    ),
    RecentNewValueFeed(
        id = 7487522L,
        url = "https://media.rss.com/pride-and-prejudice-by-jane-austen-book-summary-podcast-english/feed.xml",
        title = "Pride and Prejudice by Jane Austen, Book Summary, Podcast, English",
        author = "Raghvendra Singh",
        image = "https://media.rss.com/pride-and-prejudice-by-jane-austen-book-summary-podcast-english/20250915_050930_56a376a613c5196b0196aae49cede74e.png",
        newestItemPublishTime = Instant.fromEpochSeconds(1757957773L),
        itunesId = null,
        trendScore = 0,
        language = "en",
        categories = listOf(
            Category.ARTS,
            Category.COMEDY
        )
    )
)

val recentNewValueFeedTestData = recentNewValueFeedTestDataList.first()

val soundbiteTestDataList = listOf(
    Soundbite(
        enclosureUrl = "https://www.buzzsprout.com/58912/episodes/17851236-the-power-of-being-planted.mp3",
        title = "",
        startTime = Instant.fromEpochSeconds(922L),
        duration = 21L.seconds,
        episodeId = 42623855118L,
        episodeTitle = "The Power of Being Planted",
        feedTitle = "The Writing Worship Podcast - For Worship Songwriters",
        feedUrl = "https://feeds.buzzsprout.com/58912.rss",
        feedId = 808654L
    ),
    Soundbite(
        enclosureUrl = "https://www.buzzsprout.com/2489643/episodes/17743443-from-shadows-to-spotlight-how-women-can-shine-in-a-noisy-world.mp3",
        title = "",
        startTime = Instant.fromEpochSeconds(2380L),
        duration = 37L.seconds,
        episodeId = 42623789841L,
        episodeTitle = "From Shadows to Spotlight: How women can shine in a noisy world",
        feedTitle = "She Leads Collective Podcast: stories, allyship and confidence tools for women",
        feedUrl = "https://feeds.buzzsprout.com/2489643.rss",
        feedId = 7365610L
    ),
    Soundbite(
        enclosureUrl = "https://www.buzzsprout.com/2461021/episodes/17812110-joseph-s-secret-to-waiting-well-staying-faithful-and-speaking-truth-even-when-it-s-hard.mp3",
        title = "",
        startTime = Instant.fromEpochSeconds(1023L),
        duration = 59L.seconds,
        episodeId = 42623762000L,
        episodeTitle = "Joseph's Secret to Waiting Well: Staying Faithful and Speaking Truth Even When it's Hard",
        feedTitle = "FR, Letʼs Talk: Real Talk + Biblical Truth for Christian Teen Girls",
        feedUrl = "https://feeds.buzzsprout.com/2461021.rss",
        feedId = 7262795L
    ),
    Soundbite(
        enclosureUrl = "https://www.buzzsprout.com/1804073/episodes/17828517-eclipse-season-upgrades-how-the-sept-21st-solar-eclipse-will-transform-your-life-with-jennifer-pilates.mp3",
        title = "",
        startTime = Instant.fromEpochSeconds(137L),
        duration = 25L.seconds,
        episodeId = 42623754421L,
        episodeTitle = "Eclipse Season Upgrades: How the Sept 21st Solar Eclipse Will Transform Your Life with Jennifer Pilates",
        feedTitle = "Empowered Within with Jennifer Pilates – Spiritual Growth | Inner Empowerment | Transforming Mind-Body-Spirit",
        feedUrl = "https://feeds.buzzsprout.com/1804073.rss",
        feedId = 3747405L
    ),
    Soundbite(
        enclosureUrl = "https://www.buzzsprout.com/2260646/episodes/17830835-wplg-local-10-breaks-free-a-new-era-in-south-florida-broadcasting.mp3",
        title = "",
        startTime = Instant.fromEpochSeconds(241L),
        duration = 41L.seconds,
        episodeId = 42623738294L,
        episodeTitle = "WPLG Local 10 Breaks Free: A New Era in South Florida Broadcasting",
        feedTitle = "Tales From South Florida",
        feedUrl = "https://feeds.buzzsprout.com/2260646.rss",
        feedId = 6730992L
    ),
    Soundbite(
        enclosureUrl = "https://www.buzzsprout.com/2258174/episodes/17767374-human-in-the-loop-ai-in-the-mix-building-teams-that-can-still-think-with-mark-evans-ai-special-part-3.mp3",
        title = "",
        startTime = Instant.fromEpochSeconds(864L),
        duration = 30L.seconds,
        episodeId = 42623732730L,
        episodeTitle = "Human in the Loop, AI in the Mix: Building Teams That Can Still Think — with Mark Evans (AI Special - Part 3)",
        feedTitle = "The Elephant in the Org",
        feedUrl = "https://feeds.buzzsprout.com/2258174.rss",
        feedId = 6661182L
    ),
    Soundbite(
        enclosureUrl = "https://www.buzzsprout.com/1961832/episodes/17490971-an-unknown-burden-drug-resistance-and-lab-capacity-in-africa.mp3",
        title = "",
        startTime = Instant.fromEpochSeconds(791L),
        duration = 60L.seconds,
        episodeId = 42623723367L,
        episodeTitle = "An Unknown Burden – Drug resistance and lab capacity in Africa",
        feedTitle = "One World, One Health",
        feedUrl = "https://feeds.buzzsprout.com/1961832.rss",
        feedId = 5476158L
    ),
    Soundbite(
        enclosureUrl = "https://www.buzzsprout.com/1797429/episodes/17855765-how-to-say-no-to-customers-when-growing-your-product-patrick-ndjientcheu-chief-product-and-engineering-team-irembo.mp3",
        title = "",
        startTime = Instant.fromEpochSeconds(719L),
        duration = 60L.seconds,
        episodeId = 42623704487L,
        episodeTitle = "How to say no to customers when growing your product - Patrick Ndjientcheu (Chief Product and Engineering Team, Irembo)",
        feedTitle = "The Product Experience",
        feedUrl = "https://feeds.buzzsprout.com/1797429.rss",
        feedId = 1156837L
    ),
    Soundbite(
        enclosureUrl = "https://pdcn.co/e/www.buzzsprout.com/1929525/episodes/17855749-s7-ep-2-i-thought-i-was-doing-it-all-wrong.mp3",
        title = "",
        startTime = Instant.fromEpochSeconds(510L),
        duration = 43L.seconds,
        episodeId = 42623455658L,
        episodeTitle = "S7, Ep 2: \"I thought I was doing it all wrong\"",
        feedTitle = "Stepmum Space",
        feedUrl = "https://feeds.buzzsprout.com/1929525.rss",
        feedId = 5034029L
    ),
    Soundbite(
        enclosureUrl = "https://www.buzzsprout.com/2460778/episodes/17858404-mania-abroad-and-spice-cast-ep-25.mp3",
        title = "",
        startTime = Instant.fromEpochSeconds(4013L),
        duration = 60L.seconds,
        episodeId = 42622927013L,
        episodeTitle = "Mania Abroad and Spice Cast [Ep. 25]",
        feedTitle = "Nerd the Fuck Out",
        feedUrl = "https://feeds.buzzsprout.com/2460778.rss",
        feedId = 7253116L
    )
)

val soundbiteTestData = soundbiteTestDataList.first()