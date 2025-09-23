package io.jacob.episodive.core.testing.model

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
        id = 7230762L,
        url = "https://feeds.buzzsprout.com/2456186.rss",
        title = "Strength Of The Message Podcast with Jerry Curl",
        description = "<p>Join host Jerry Curl as he empowers individuals with clarity, fresh insights, and actionable strategies to cultivate a transformative mindset—enhancing the mind, body, and spirit—not just to achieve success, but to sustain it and continuously evolve into their best future selves.</p>",
        author = "Jerry Curl",
        image = "https://storage.buzzsprout.com/bg4e9pz4t0xzfmn5zr1k386zr9fc?.jpg",
        artwork = "https://storage.buzzsprout.com/bg4e9pz4t0xzfmn5zr1k386zr9fc?.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758603600L),
        itunesId = 1798857111L,
        trendScore = 9,
        language = "en-us",
        categories = listOf(
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT
        )
    ),
    TrendingFeed(
        id = 7258341L,
        url = "https://feeds.transistor.fm/the-secure-husband",
        title = "The Secure Husband",
        description = "A Secure Husband no longer seeks validation from his wife—he stands strong in self-worth, meets his own emotional needs, and leads with confidence and clarity. I've been where you are, and I'm here to help you break free from old patterns, reclaim your strength, and transform your marriage from the inside out.",
        author = "M. Bruce Abbott, M.A, CPC",
        image = "https://img.transistor.fm/HkPRzX_jb9NYxmQFwZYB3MTDKZMPMXQLTUan_DkdLjE/rs:fill:0:0:1/w:1400/h:1400/q:60/mb:500000/aHR0cHM6Ly9pbWct/dXBsb2FkLXByb2R1/Y3Rpb24udHJhbnNp/c3Rvci5mbS8xM2Vj/NGY4MTMxZDhhNmE5/ZGI4YzVmMjNmZjRh/YWU4MC5wbmc.jpg",
        artwork = "https://img.transistor.fm/HkPRzX_jb9NYxmQFwZYB3MTDKZMPMXQLTUan_DkdLjE/rs:fill:0:0:1/w:1400/h:1400/q:60/mb:500000/aHR0cHM6Ly9pbWct/dXBsb2FkLXByb2R1/Y3Rpb24udHJhbnNp/c3Rvci5mbS8xM2Vj/NGY4MTMxZDhhNmE5/ZGI4YzVmMjNmZjRh/YWU4MC5wbmc.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758603600L),
        itunesId = 1802869543L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT,
            Category.HEALTH,
            Category.FITNESS,
            Category.MENTAL
        )
    ),
    TrendingFeed(
        id = 1114497L,
        url = "http://feeds.ancientfaith.com/WholeCounselOfGod",
        title = "The Whole Counsel of God",
        description = "This podcast takes us through the Holy Scriptures in a verse by verse study based on the Great Tradition of the Orthodox Church.",
        author = "Fr. Stephen De Young, and Ancient Faith Ministries",
        image = "https://www.ancientfaith.com/media/1800/WholeCounselOfGod2.jpeg",
        artwork = "https://www.ancientfaith.com/media/1800/WholeCounselOfGod2.jpeg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758603601L),
        itunesId = 1193439458L,
        trendScore = 9,
        language = "en-us",
        categories = listOf(
            Category.RELIGION,
            Category.SPIRITUALITY,
            Category.CHRISTIANITY
        )
    ),
    TrendingFeed(
        id = 5443082L,
        url = "https://feeds.360.audion.fm/5upnC-Vw80jZBNBridl-6",
        title = "Chaleur Humaine",
        description = "<p>Comment faire face au défi climatique ? Tous les mardis, Nabil Wakim et la rédaction du Monde invitent des experts pour comprendre les enjeux et trouver des solutions.<br/>Chaleur Humaine est aussi une newsletter. Inscrivez-vous pour recevoir chaque mardi notre sélection d'articles : https://www.lemonde.fr/newsletters/chaleur-humaine/<br/></p><p> <br />Hébergé par Audion. Visitez <a href=\"https://www.audion.fm/fr/privacy-policy\" rel=\"noopener noreferrer\" target=\"_blank\">https://www.audion.fm/fr/privacy-policy</a> pour plus d'informations.</p>",
        author = "Le Monde",
        image = "https://artworks.360.audion.fm/f08cb57d-2b39-41d6-a631-5bf8793dbeba.jpg?width=3000&height=3000",
        artwork = "https://artworks.360.audion.fm/f08cb57d-2b39-41d6-a631-5bf8793dbeba.jpg?width=3000&height=3000",
        newestItemPublishTime = Instant.fromEpochSeconds(1758603601L),
        itunesId = 1622382158L,
        trendScore = 9,
        language = "fr",
        categories = listOf(
            Category.NEWS,
            Category.SOCIETY,
            Category.CULTURE
        )
    ),
    TrendingFeed(
        id = 511163L,
        url = "https://albertmohler.com/category/the-briefing/feed/?utm_medium=referral&utm_source=itunes&utm_campaign=the_briefing",
        title = "The Briefing with Albert Mohler",
        description = "For more resources, including articles, The Briefing, Thinking in Public, and archived editions of his nationally-syndicated radio show, The Albert Mohler Program, be sure to visit albertmohler.com.",
        author = "R. Albert Mohler, Jr.",
        image = "https://media.sbts.edu/sites/albertmohler.com/img/podcast-the-briefing.png",
        artwork = "https://media.sbts.edu/sites/albertmohler.com/img/podcast-the-briefing.png",
        newestItemPublishTime = Instant.fromEpochSeconds(1758603603L),
        itunesId = 390278738L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.RELIGION,
            Category.SPIRITUALITY,
            Category.CHRISTIANITY
        )
    ),
    TrendingFeed(
        id = 7260880L,
        url = "https://www.spreaker.com/show/6559862/episodes/feed",
        title = "Daily Motivation | Morning Affirmations",
        description = "Need motivation to start your day?<br /><br />This podcast delivers it in minutes! Daily Motivation Mornings brings you quick, high-energy affirmations to boost confidence, ignite success, and crush your goals.<br /><br />Whether you need inspiration, focus, or a mindset reset, these short morning episodes will get you moving. Wake up motivated—every day!<br /><br />Subscribe now for your daily dose of motivation. Follow @NovosPositivity for extra inspiration.",
        author = "Motivation",
        image = "https://d3wo5wojvuv7l.cloudfront.net/t_rss_itunes_square_1400/images.spreaker.com/original/c1c588743205f134f385a524be2a54b6.jpg",
        artwork = "https://d3wo5wojvuv7l.cloudfront.net/t_rss_itunes_square_1400/images.spreaker.com/original/c1c588743205f134f385a524be2a54b6.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758603607L),
        itunesId = 1803268777L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.HEALTH,
            Category.FITNESS,
            Category.MENTAL,
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT
        )
    ),
    TrendingFeed(
        id = 7312321L,
        url = "https://www.spreaker.com/show/6604694/episodes/feed",
        title = "Addiction-Proof | Master Dopamine & Focus Again",
        description = "Be unbreakable. Be addiction-proof.<br /><br />Addiction-Proof trains you to rebuild your mental resilience, strengthen focus, and live free from the constant need for stimulation.<br /><br />🛡️ For self-masters, creators, and changemakers.<br />✅ Detox your dopamine system<br />✅ Rebuild emotional regulation<br />✅ Live addiction-free—and powerful<br /><br />Freedom starts with your mind.<br /><br />Follow <a href=\"https://twitter.com/NovosPositivity\" target=\"_blank\" rel=\"noreferrer noopener\">@NovosPositivity</a> for focus and resilience tips.",
        author = "Motivation",
        image = "https://d3wo5wojvuv7l.cloudfront.net/t_rss_itunes_square_1400/images.spreaker.com/original/6d094295fb7dba02937ff9eec1500945.jpg",
        artwork = "https://d3wo5wojvuv7l.cloudfront.net/t_rss_itunes_square_1400/images.spreaker.com/original/6d094295fb7dba02937ff9eec1500945.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758603607L),
        itunesId = 1811067562L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.HEALTH,
            Category.FITNESS,
            Category.MENTAL,
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT
        )
    ),
    TrendingFeed(
        id = 7328975L,
        url = "https://www.spreaker.com/show/6617129/episodes/feed",
        title = "Daily Stoic Edge | Stay Sharp, Stay Grounded",
        description = "Feeling stressed, overwhelmed, or lost in the chaos of daily life? The Daily Stoic Edge is here to help you stay sharp, grounded, and resilient in the face of any challenge.<br /><br />Drawing inspiration from the timeless wisdom of Stoicism, this podcast delivers practical insights, actionable exercises, and inspiring stories to help you navigate modern life with clarity, purpose, and inner peace. Each episode equips you with tools to manage your emotions, cultivate resilience, make ethical decisions, and live a life of meaning and fulfillment. <br /><br />Whether you're seeking greater self-awareness, improved focus, or simply a sense of calm amidst the storm, The Daily Stoic Edge will be your guide. Join us as we explore the power of Stoicism to enhance your well-being, sharpen your mind, and live with greater intentionality every day.",
        author = "Peak Performance",
        image = "https://d3wo5wojvuv7l.cloudfront.net/t_rss_itunes_square_1400/images.spreaker.com/original/da74dff21fa49dc668d34ddddd75ec2f.jpg",
        artwork = "https://d3wo5wojvuv7l.cloudfront.net/t_rss_itunes_square_1400/images.spreaker.com/original/da74dff21fa49dc668d34ddddd75ec2f.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758603607L),
        itunesId = 1813503632L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.HEALTH,
            Category.FITNESS,
            Category.MENTAL,
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT
        )
    ),
    TrendingFeed(
        id = 7328977L,
        url = "https://www.spreaker.com/show/6617068/episodes/feed",
        title = "Daily Stoicism | Power, Presence & Perspective",
        description = "Feeling overwhelmed by modern life? Struggling to find peace amidst chaos? 'Daily Stoicism: Power, Presence & Perspective' is your guide to living a more resilient and fulfilling life.<br /><br />Each day, we delve into the timeless wisdom of Stoic philosophy, exploring its practical applications for navigating challenges, cultivating inner strength, and finding meaning in everyday experiences. Through insightful meditations, thought-provoking discussions, and actionable strategies, you'll learn how to:<br /><br />Master your emotions: Discover the power of self-control and develop resilience in the face of adversity.<br />Live with purpose: Align your actions with your values and cultivate a sense of fulfillment.<br />Find clarity & perspective:  Gain a deeper understanding of yourself and the world around you.<br /><br />Whether you're seeking personal growth, stress management techniques, or simply a renewed sense of purpose, 'Daily Stoicism: Power, Presence & Perspective' will empower you to live a life of virtue, wisdom, and tranquility.<br /><br /><br />Join us on this transformative journey – start your day with Stoic strength!",
        author = "Peak Performance",
        image = "https://d3wo5wojvuv7l.cloudfront.net/t_rss_itunes_square_1400/images.spreaker.com/original/6075a71767391144350a320009dba6ee.jpg",
        artwork = "https://d3wo5wojvuv7l.cloudfront.net/t_rss_itunes_square_1400/images.spreaker.com/original/6075a71767391144350a320009dba6ee.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758603607L),
        itunesId = 1813503971L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.HEALTH,
            Category.FITNESS,
            Category.MENTAL,
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT
        )
    ),
    TrendingFeed(
        id = 7328987L,
        url = "https://www.spreaker.com/show/6617106/episodes/feed",
        title = "Daily Stoic Power | Control, Calm & Clarity Hacks",
        description = "Feeling stressed, anxious, or overwhelmed by life's chaos? It's time to reclaim your inner peace and power with 'Daily Stoic Power: Control, Calm & Clarity Hacks'.<br /><br />This podcast delivers bite-sized wisdom from ancient Stoic philosophy, providing you with practical tools and techniques to navigate challenges, cultivate resilience, and live a more meaningful life. Each day, discover powerful insights on managing emotions, finding clarity amidst chaos, and cultivating a sense of inner control. <br /><br />Whether you're facing work pressure, relationship difficulties, or simply seeking a path to greater well-being, 'Daily Stoic Power' offers guidance and inspiration to help you embody the timeless principles of Stoicism for modern life. Start your journey towards emotional strength, mental clarity, and lasting peace.<br /><br />Subscribe today and unlock the power within!",
        author = "Peak Performance",
        image = "https://d3wo5wojvuv7l.cloudfront.net/t_rss_itunes_square_1400/images.spreaker.com/original/bd16b12a3e267245c81fbcd693c85e23.jpg",
        artwork = "https://d3wo5wojvuv7l.cloudfront.net/t_rss_itunes_square_1400/images.spreaker.com/original/bd16b12a3e267245c81fbcd693c85e23.jpg",
        newestItemPublishTime = Instant.fromEpochSeconds(1758603607L),
        itunesId = 1813506343L,
        trendScore = 9,
        language = "en",
        categories = listOf(
            Category.HEALTH,
            Category.FITNESS,
            Category.MENTAL,
            Category.EDUCATION,
            Category.SELF_IMPROVEMENT
        )
    )
)

val trendingFeedTestData = trendingFeedTestDataList.first()

val recentFeedTestDataList = listOf(
    RecentFeed(
        id = 5499546L,
        url = "https://voiceofourmother.cn/feed/podcast/",
        title = "Radio Maria Chinese Mandarin",
        newestItemPublishTime = Instant.fromEpochSeconds(1758629379L),
        oldestItemPublishTime = Instant.fromEpochSeconds(1753634233L),
        description = "效法慈母信德 传递信仰之声",
        image = "https://radiomaria.cn/wp-content/uploads/powerpress/avatars-QSjqed7AMKv9gKP7-s6bCUQ-original.jpg",
        itunesId = 1625715900L,
        language = "zh-CN",
        categories = listOf(Category.RELIGION, Category.SPIRITUALITY)
    ),
    RecentFeed(
        id = 7354020L,
        url = "https://media.rss.com/ai-fire-daily/feed.xml",
        title = "AI Fire Daily",
        newestItemPublishTime = Instant.fromEpochSeconds(1758629310L),
        oldestItemPublishTime = Instant.fromEpochSeconds(1748507477L),
        description = "<p><strong>AI Fire</strong> – Master AI with practical guides. Your daily hub for AI-powered productivity. Join 72,000+ professionals from Google, Meta, Microsoft, Tesla, and more.</p><p><strong>AI Fire Podcast</strong> is your go-to resource for everything AI, from the latest trends to how AI can transform your career. Hosted by the <strong>AI Fire</strong> team and AI enthusiasts, we focus on providing you with practical tips to boost your productivity using AI tools and strategies.</p><p><strong>Our mission</strong> is to help you keep up with AI trends, master new skills, and get more done in less time. Whether you're looking to make money with AI, dive into prompt engineering, or explore automation and AI workflows, we've got you covered.</p><ul><li><strong>Sign up for our FREE daily newsletter</strong>: <a target=\"_blank\" rel=\"noopener noreferrer nofollow\" href=\"https://www.aifire.co/subscribe\">https://www.aifire.co/subscribe</a></li><li><strong>Get 3-level AI tutorials across industries in our Community</strong>: <a target=\"_blank\" rel=\"noopener noreferrer nofollow\" href=\"https://community.aifire.co/\">https://community.aifire.co/</a></li><li><strong>Join AI Fire Academy</strong>: 500+ advanced AI workflows ($14,500+ Value): <a target=\"_blank\" rel=\"noopener noreferrer nofollow\" href=\"https://www.aifire.co/upgrade\">https://www.aifire.co/upgrade</a></li><li><strong>Email us</strong>: <a target=\"_blank\" rel=\"noopener noreferrer nofollow\" href=\"mailto:services@aifire.co\">services@aifire.co</a></li></ul><p>In the <strong>AI Fire Podcast</strong>, we cover a wide range of topics such as:</p><ul><li><strong>How to make money with AI</strong></li><li><strong>AI Tools</strong> and <strong>AI Jobs</strong></li><li><strong>Prompt Engineering</strong></li><li><strong>AI Automations</strong> and <strong>AI Workflows</strong></li><li><strong>AI Research</strong> and <strong>AI Reports</strong></li><li><strong>AI Case Studies</strong></li><li><strong>AI Startups</strong> and <strong>AI Books</strong></li><li><strong>AI Grants and Deals</strong></li></ul><p><strong>Our Socials:</strong></p><ul><li><strong>Facebook Group</strong>: Join 212K+ AI builders – <a target=\"_blank\" rel=\"noopener noreferrer nofollow\" href=\"https://www.facebook.com/groups/aifire.co\">https://www.facebook.com/groups/aifire.co</a></li><li><strong>X (Twitter)</strong>: Follow us for daily AI drops – <a target=\"_blank\" rel=\"noopener noreferrer nofollow\" href=\"https://x.com/aifireco\">https://x.com/aifireco</a></li><li><strong>YouTube</strong>: Watch AI walkthroughs & tutorials – <a target=\"_blank\" rel=\"noopener noreferrer nofollow\" href=\"https://www.youtube.com/@aifire.official\">https://www.youtube.com/@aifire.official</a></li></ul>",
        image = "https://media.rss.com/ai-fire-daily/20250604_090646_e7b928bbae0190271124179ee55fb4ce.jpg",
        itunesId = 1817366984L,
        language = "en",
        categories = listOf(Category.TECHNOLOGY, Category.BUSINESS)
    ),
    RecentFeed(
        id = 1065592L,
        url = "https://feeds.simplecast.com/MBdw4Qrw",
        title = "Les Grandes Gueules",
        newestItemPublishTime = Instant.fromEpochSeconds(1758629289L),
        oldestItemPublishTime = Instant.fromEpochSeconds(1757333584L),
        description = "Les Grandes Gueules, le talk-show de la liberté d'expression, autour du duo Alain Marschall/Olivier Truchot. Trois heures de débats parfois musclés avec vos GG toutes issues de la société civile : médecin, éleveur, prof, chef d'entreprise, fonctionnaire, avocat... L'actualité vue et commentée par des Grandes Gueules qui défendent leurs idées, points de vues, opinions toujours variées ! Et par les auditeurs du 3216 qui n'hésitent pas à rappeler nos GG à l'ordre !",
        image = "https://image.simplecastcdn.com/images/9ccd3bc4-0895-402d-a683-1fc4d0495dd5/448960f1-cbc7-4ab3-9049-910d81b77986/3000x3000/1400-podcasts-grandesgueules-2025.jpg?aid=rss_feed",
        itunesId = 82354094L,
        language = "fr",
        categories = listOf(Category.NEWS, Category.COMMENTARY)
    ),
    RecentFeed(
        id = 6838264L,
        url = "https://feeds.simplecast.com/HeUMrPAA",
        title = "Thought Digest with Caiah & Mpho",
        newestItemPublishTime = Instant.fromEpochSeconds(1758629278L),
        oldestItemPublishTime = Instant.fromEpochSeconds(1710672000L),
        description = "Thought Digest is a meticulously curated podcast hosted by two accomplished young Black women, Caiah and Mpho. With a steadfast commitment to delivering a multifaceted experience, the podcast transcends conventional boundaries by seamlessly blending personal narratives with rigorously researched insights from esteemed sources. Designed to captivate a broad audience demographic, Thought Digest endeavors to serve as a beacon of enlightenment, entertainment, and empowerment. By fostering candid conversations and promoting critical thinking, Caiah and Mpho aspire to not only engage listeners but also to inspire meaningful discourse that resonates long after each episode concludes.",
        image = "https://image.simplecastcdn.com/images/0d84edad-ed99-4a34-a3d9-6f9733e08a17/ed47520f-5113-43e1-b555-561c8fc80b53/3000x3000/b043be82-a388-460e-a26a-a211f758fa1b.jpg?aid=rss_feed",
        itunesId = 1736312125L,
        language = "en",
        categories = listOf(
            Category.SOCIETY,
            Category.CULTURE,
            Category.PERSONAL,
            Category.JOURNALS
        )
    ),
    RecentFeed(
        id = 153470L,
        url = "https://feeds.transistor.fm/cronica-da-cidade",
        title = "Crônica da Cidade",
        newestItemPublishTime = Instant.fromEpochSeconds(1758629259L),
        oldestItemPublishTime = Instant.fromEpochSeconds(1608301240L),
        description = "Crônica da Cidade com o jornalista Carlos Pereira.",
        image = "https://img.transistor.fm/9APx99YvxmXDlRKN5j3mLjUfeAqLpCs0F5SFedkOt7o/rs:fill:0:0:1/w:1400/h:1400/q:60/mb:500000/aHR0cHM6Ly9pbWct/dXBsb2FkLXByb2R1/Y3Rpb24udHJhbnNp/c3Rvci5mbS9zaG93/LzEyMjE1LzE1OTQw/NTc5MDktYXJ0d29y/ay5qcGc.jpg",
        itunesId = 1522918131L,
        language = "pt",
        categories = listOf(
            Category.SOCIETY,
            Category.CULTURE,
            Category.PERSONAL,
            Category.JOURNALS
        )
    ),
    RecentFeed(
        id = 4545905L,
        url = "https://feed.pod.space/norrteljetidningssportpoddar",
        title = "Från Malmen till Schaktet",
        newestItemPublishTime = Instant.fromEpochSeconds(1758629237L),
        oldestItemPublishTime = Instant.fromEpochSeconds(1637770511L),
        description = "<div>Podcast om fotbollen i Norrtälje Kommun som görs av sportredaktionen på Norrtelje Tidning.<br><strong>Ansvarig utgivare: </strong>Christian Höijer.<br><strong>Kontakta redaktionen: </strong>martin.lidholm@bonniernews.se.<br><strong>Annonsförfrågningar:</strong> <a href=\"mailto:podcast.sales@bonniernews.se\">podcast.sales@bonniernews.se</a>.<br><br></div>",
        image = "https://assets.pod.space/system/shows/images/c64/3cc/ab-/large/NT_fotbollspodden_1400x1400.jpg",
        itunesId = 1572674203L,
        language = "sv",
        categories = listOf(Category.SPORTS)
    ),
    RecentFeed(
        id = 6337190L,
        url = "https://media.rss.com/not-just-a-rainbow/feed.xml",
        title = "Not Just a Rainbow",
        newestItemPublishTime = Instant.fromEpochSeconds(1758629209L),
        oldestItemPublishTime = Instant.fromEpochSeconds(1682596628L),
        description = "<p>The LGBTQ+ community has had to grapple with the social and legal difficulties of being I am still fortunate enough to have some of the most influential LGBTQ+ figures alive today. This inspired me to create this podcast. I am Catherine Cager who explores the true meaning of being LGBTQ+ in a world that is rapidly changing.</p>",
        image = "https://media.rss.com/not-just-a-rainbow/20230425_010430_77598041951acc8f2df22593ecb51ca3.jpg",
        itunesId = null,
        language = "en",
        categories = listOf(
            Category.SOCIETY,
            Category.CULTURE,
            Category.RELATIONSHIPS,
            Category.HEALTH,
            Category.FITNESS,
            Category.SEXUALITY
        )
    ),
    RecentFeed(
        id = 7347537L,
        url = "https://feeds.captivate.fm/medical-device/",
        title = "Medical Device Global Market Access",
        newestItemPublishTime = Instant.fromEpochSeconds(1758629153L),
        oldestItemPublishTime = Instant.fromEpochSeconds(1748001660L),
        description = "Navigate every market. Accelerate every launch.\nMedical Device Global Market Access by Pure Global is the audio briefing that turns the world's most confusing regulatory pathways into clear, actionable roadmaps. If you're a MedTech founder, RA/QA leader, product manager, or investor who needs to get devices cleared anywhere from Austin to Abu Dhabi, this show is your shortcut.\nWhy listen?\n\t•\tStep-by-step playbooks – We decode EU MDR, U.S. 510(k), Brazil's ANVISA, China's NMPA, and 25 + other regimes, showing exactly how long each milestone really takes and where companies get stuck.\n\t•\tFirst-hand war stories – Hear candid interviews with regulatory veterans who have shepherded implants, software, wearables, diagnostics, and AI algorithms to market—and lived to tell the tale.\n\t•\tReal-time intelligence – Every episode covering the week's new guidances, standards, and enforcement trends so you're always ahead of the curve.\n\t•\tActionable templates – From clinical evaluation checklists to technical-file skeletons, we break down the documents you'll need and the pitfalls reviewers love to flag.\n\t•\tAI-powered insights – Because Pure Global mines millions of public filings, tenders, and recalls, we surface data-backed best practices you won't hear at conferences—or from competitors.\nWhat you'll hear\n\t•\tLaunching SaMD under multiple risk classes in parallel\n\t•\tSurviving an unannounced ISO 13485 audit\n\t•\tBuilding a reg-first QMS that scales\n\t•\tMastering Latin-American registrations without endless language cycles\n\t•\tLeveraging real-world evidence to shorten clinical timelines\n\t•\tAligning cybersecurity, privacy, and post-market surveillance rules across regions\nWhether you're plotting first entry, managing lifecycle expansions, or rescuing a delayed submission, subscribing could shave months—and millions—off your path to revenue.\n⸻\nBrought to you by Pure Global – the AI-native consultancy that already guides MedTech innovators through regulatory and market-access hurdles in 30 + countries. Ready for expert help on your next submission? Visit https://pureglobal.com or email info@pureglobal.com to start accelerating today.",
        image = "https://artwork.captivate.fm/0344be0f-5043-4e20-9129-b0c5cd358378/qdPAlMI3Ob6iVzfjLVCyJb4A.jpg",
        itunesId = 1816370207L,
        language = "en",
        categories = listOf(Category.TECHNOLOGY)
    ),
    RecentFeed(
        id = 6363500L,
        url = "https://feeds.castos.com/nz3mx",
        title = "Maurais Live",
        newestItemPublishTime = Instant.fromEpochSeconds(1758629135L),
        oldestItemPublishTime = Instant.fromEpochSeconds(1758196351L),
        description = "Avec Dominic Maurais, Dan Gravel et Dominique Dumas",
        image = "https://episodes.castos.com/66b0d35a1fe7a3-51883808/images/podcast/covers/c1a-kqx95-ndzdxm20s90-8z4qvh.jpg",
        itunesId = 1686151710L,
        language = "fr-CA",
        categories = listOf(
            Category.NEWS,
            Category.SOCIETY,
            Category.CULTURE,
            Category.PERSONAL,
            Category.JOURNALS
        )
    ),
    RecentFeed(
        id = 7449897L,
        url = "https://feeds.transistor.fm/aktualno",
        title = "Aktualno",
        newestItemPublishTime = Instant.fromEpochSeconds(1758629127L),
        oldestItemPublishTime = Instant.fromEpochSeconds(1735938000L),
        description = "Vsak dan se v oddaji Aktualno lotimo trenutno najaktualnejše teme, včasih tudi dveh, z vseh možnih področij, najsi gre za kulturo, gospodarstvo, politiko, šport ali kaj drugega. Oddaja, običajno v obliki intervjuja, prinaša tudi odgovore na najbolj „vroča vprašanja.",
        image = "https://img.transistor.fm/UHpzxlbEzVO_VFSCHBF3Yb_GNOT31CtPRL_-RsTNztM/rs:fill:0:0:1/w:1400/h:1400/q:60/mb:500000/aHR0cHM6Ly9pbWct/dXBsb2FkLXByb2R1/Y3Rpb24udHJhbnNp/c3Rvci5mbS84ODA3/YzZhY2M4YjZmYjBk/ZTc3ODI3M2Q1MGE3/MzU3MC5qcGc.jpg",
        itunesId = null,
        language = "sl",
        categories = listOf(Category.NEWS, Category.SOCIETY, Category.CULTURE)
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