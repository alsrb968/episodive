package io.jacob.episodive.core.model

enum class Category(val id: Int, val label: String, val imageUrl: String) {
    ARTS(1, "Arts", "https://images.pexels.com/photos/1727658/pexels-photo-1727658.jpeg"),
    BOOKS(
        2,
        "Books",
        "https://images.pexels.com/photos/159711/books-bookstore-book-reading-159711.jpeg"
    ),
    DESIGN(3, "Design", "https://images.pexels.com/photos/1029611/pexels-photo-1029611.jpeg"),
    FASHION(4, "Fashion", "https://images.pexels.com/photos/1154861/pexels-photo-1154861.jpeg"),
    BEAUTY(5, "Beauty", "https://images.pexels.com/photos/33989227/pexels-photo-33989227.jpeg"),
    FOOD(6, "Food", "https://images.pexels.com/photos/1640772/pexels-photo-1640772.jpeg"),
    PERFORMING(
        7,
        "Performing",
        "https://images.pexels.com/photos/1763075/pexels-photo-1763075.jpeg"
    ),
    VISUAL(8, "Visual", "https://images.pexels.com/photos/3685175/pexels-photo-3685175.jpeg"),
    BUSINESS(9, "Business", "https://images.pexels.com/photos/1342609/pexels-photo-1342609.jpeg"),
    CAREERS(10, "Careers", "https://images.pexels.com/photos/7693223/pexels-photo-7693223.jpeg"),
    ENTREPRENEURSHIP(
        11,
        "Entrepreneurship",
        "https://images.pexels.com/photos/3184405/pexels-photo-3184405.jpeg"
    ),
    INVESTING(12, "Investing", "https://images.pexels.com/photos/159888/pexels-photo-159888.jpeg"),
    MANAGEMENT(
        13,
        "Management",
        "https://images.pexels.com/photos/3285203/pexels-photo-3285203.jpeg"
    ),
    MARKETING(14, "Marketing", "https://images.pexels.com/photos/267350/pexels-photo-267350.jpeg"),
    NON_PROFIT(
        15,
        "Non-Profit",
        "https://images.pexels.com/photos/8035133/pexels-photo-8035133.jpeg"
    ),
    COMEDY(16, "Comedy", "https://images.pexels.com/photos/3377788/pexels-photo-3377788.jpeg"),
    INTERVIEWS(
        17,
        "Interviews",
        "https://images.pexels.com/photos/2041396/pexels-photo-2041396.jpeg"
    ),
    IMPROV(18, "Improv", "https://images.pexels.com/photos/12903983/pexels-photo-12903983.jpeg"),
    STAND_UP(19, "Stand-Up", "https://images.pexels.com/photos/144429/pexels-photo-144429.jpeg"),
    EDUCATION(
        20,
        "Education",
        "https://images.pexels.com/photos/159844/cellular-education-classroom-159844.jpeg"
    ),
    COURSES(21, "Courses", "https://images.pexels.com/photos/7092350/pexels-photo-7092350.jpeg"),
    HOW_TO(22, "How-To", "https://images.pexels.com/photos/7253877/pexels-photo-7253877.jpeg"),
    LANGUAGE(23, "Language", "https://images.pexels.com/photos/267669/pexels-photo-267669.jpeg"),
    LEARNING(24, "Learning", "https://images.pexels.com/photos/5427862/pexels-photo-5427862.jpeg"),
    SELF_IMPROVEMENT(
        25,
        "Self-Improvement",
        "https://images.pexels.com/photos/34006937/pexels-photo-34006937.jpeg"
    ),
    FICTION(26, "Fiction", "https://images.pexels.com/photos/1314584/pexels-photo-1314584.jpeg"),
    DRAMA(27, "Drama", "https://images.pexels.com/photos/6800202/pexels-photo-6800202.jpeg"),
    HISTORY(
        28,
        "History",
        "https://images.pexels.com/photos/36006/renaissance-schallaburg-figures-facade.jpg"
    ),
    HEALTH(29, "Health", "https://images.pexels.com/photos/317157/pexels-photo-317157.jpeg"),
    FITNESS(30, "Fitness", "https://images.pexels.com/photos/841130/pexels-photo-841130.jpeg"),
    ALTERNATIVE(
        31,
        "Alternative",
        "https://images.pexels.com/photos/33971865/pexels-photo-33971865.jpeg"
    ),
    MEDICINE(
        32,
        "Medicine",
        "https://images.pexels.com/photos/161449/medical-tablets-pills-drug-161449.jpeg"
    ),
    MENTAL(33, "Mental", "https://images.pexels.com/photos/3094215/pexels-photo-3094215.jpeg"),
    NUTRITION(
        34,
        "Nutrition",
        "https://images.pexels.com/photos/1640775/pexels-photo-1640775.jpeg"
    ),
    SEXUALITY(
        35,
        "Sexuality",
        "https://images.pexels.com/photos/4980381/pexels-photo-4980381.jpeg"
    ),
    KIDS(36, "Kids", "https://images.pexels.com/photos/1001914/pexels-photo-1001914.jpeg"),
    FAMILY(37, "Family", "https://images.pexels.com/photos/1682497/pexels-photo-1682497.jpeg"),
    PARENTING(
        38,
        "Parenting",
        "https://images.pexels.com/photos/3171118/pexels-photo-3171118.jpeg"
    ),
    PETS(39, "Pets", "https://images.pexels.com/photos/3628100/pexels-photo-3628100.jpeg"),
    ANIMALS(40, "Animals", "https://images.pexels.com/photos/198162/pexels-photo-198162.jpeg"),
    STORIES(41, "Stories", "https://images.pexels.com/photos/1741231/pexels-photo-1741231.jpeg"),
    LEISURE(42, "Leisure", "https://images.pexels.com/photos/416676/pexels-photo-416676.jpeg"),
    ANIMATION(
        43,
        "Animation",
        "https://images.pexels.com/photos/163077/mario-yoschi-figures-funny-163077.jpeg"
    ),
    MANGA(44, "Manga", "https://images.pexels.com/photos/33936978/pexels-photo-33936978.jpeg"),
    AUTOMOTIVE(
        45,
        "Automotive",
        "https://images.pexels.com/photos/1545743/pexels-photo-1545743.jpeg"
    ),
    AVIATION(
        46,
        "Aviation",
        "https://images.pexels.com/photos/45230/aircraft-double-decker-airport-army-aviation-space-45230.jpeg"
    ),
    CRAFTS(47, "Crafts", "https://images.pexels.com/photos/3145866/pexels-photo-3145866.jpeg"),
    GAMES(48, "Games", "https://images.pexels.com/photos/442576/pexels-photo-442576.jpeg"),
    HOBBIES(49, "Hobbies", "https://images.pexels.com/photos/346726/pexels-photo-346726.jpeg"),
    HOME(50, "Home", "https://images.pexels.com/photos/298842/pexels-photo-298842.jpeg"),
    GARDEN(51, "Garden", "https://images.pexels.com/photos/1055408/pexels-photo-1055408.jpeg"),
    VIDEO_GAMES(
        52,
        "Video-Games",
        "https://images.pexels.com/photos/371924/pexels-photo-371924.jpeg"
    ),
    MUSIC(53, "Music", "https://images.pexels.com/photos/1001850/pexels-photo-1001850.jpeg"),
    COMMENTARY(
        54,
        "Commentary",
        "https://images.pexels.com/photos/6878169/pexels-photo-6878169.jpeg"
    ),
    NEWS(55, "News", "https://images.pexels.com/photos/4057766/pexels-photo-4057766.jpeg"),
    DAILY(56, "Daily", "https://images.pexels.com/photos/33979136/pexels-photo-33979136.jpeg"),
    ENTERTAINMENT(
        57,
        "Entertainment",
        "https://images.pexels.com/photos/3756766/pexels-photo-3756766.jpeg"
    ),
    GOVERNMENT(
        58,
        "Government",
        "https://images.pexels.com/photos/2100942/pexels-photo-2100942.jpeg"
    ),
    POLITICS(59, "Politics", "https://images.pexels.com/photos/4669146/pexels-photo-4669146.jpeg"),
    BUDDHISM(
        60,
        "Buddhism",
        "https://images.pexels.com/photos/32831688/pexels-photo-32831688.jpeg"
    ),
    CHRISTIANITY(
        61,
        "Christianity",
        "https://images.pexels.com/photos/5874951/pexels-photo-5874951.jpeg"
    ),
    HINDUISM(62, "Hinduism", "https://images.pexels.com/photos/1707425/pexels-photo-1707425.jpeg"),
    ISLAM(63, "Islam", "https://images.pexels.com/photos/9936149/pexels-photo-9936149.jpeg"),
    JUDAISM(64, "Judaism", "https://images.pexels.com/photos/5974300/pexels-photo-5974300.jpeg"),
    RELIGION(65, "Religion", "https://images.pexels.com/photos/1278566/pexels-photo-1278566.jpeg"),
    SPIRITUALITY(
        66,
        "Spirituality",
        "https://images.pexels.com/photos/3406020/pexels-photo-3406020.jpeg"
    ),
    SCIENCE(67, "Science", "https://images.pexels.com/photos/256262/pexels-photo-256262.jpeg"),
    ASTRONOMY(68, "Astronomy", "https://images.pexels.com/photos/2156/sky-earth-space-working.jpg"),
    CHEMISTRY(69, "Chemistry", "https://images.pexels.com/photos/220989/pexels-photo-220989.jpeg"),
    EARTH(70, "Earth", "https://images.pexels.com/photos/355935/pexels-photo-355935.jpeg"),
    LIFE(71, "Life", "https://images.pexels.com/photos/1151418/pexels-photo-1151418.jpeg"),
    MATHEMATICS(
        72,
        "Mathematics",
        "https://images.pexels.com/photos/714699/pexels-photo-714699.jpeg"
    ),
    NATURAL(
        73,
        "Natural",
        "https://images.pexels.com/photos/36762/scarlet-honeyeater-bird-red-feathers.jpg"
    ),
    NATURE(74, "Nature", "https://images.pexels.com/photos/1250260/pexels-photo-1250260.jpeg"),
    PHYSICS(
        75,
        "Physics",
        "https://images.pexels.com/photos/60582/newton-s-cradle-balls-sphere-action-60582.jpeg"
    ),
    SOCIAL(76, "Social", "https://images.pexels.com/photos/696218/pexels-photo-696218.jpeg"),
    SOCIETY(77, "Society", "https://images.pexels.com/photos/1687093/pexels-photo-1687093.jpeg"),
    CULTURE(78, "Culture", "https://images.pexels.com/photos/757828/pexels-photo-757828.jpeg"),
    DOCUMENTARY(
        79,
        "Documentary",
        "https://images.pexels.com/photos/275977/pexels-photo-275977.jpeg"
    ),
    PERSONAL(80, "Personal", "https://images.pexels.com/photos/774909/pexels-photo-774909.jpeg"),
    JOURNALS(
        81,
        "Journals",
        "https://images.pexels.com/photos/34014653/pexels-photo-34014653.jpeg"
    ),
    PHILOSOPHY(
        82,
        "Philosophy",
        "https://images.pexels.com/photos/159862/art-school-of-athens-raphael-italian-painter-fresco-159862.jpeg"
    ),
    PLACES(83, "Places", "https://images.pexels.com/photos/1462935/pexels-photo-1462935.jpeg"),
    TRAVEL(84, "Travel", "https://images.pexels.com/photos/1271619/pexels-photo-1271619.jpeg"),
    RELATIONSHIPS(
        85,
        "Relationships",
        "https://images.pexels.com/photos/1667849/pexels-photo-1667849.jpeg"
    ),
    SPORTS(86, "Sports", "https://images.pexels.com/photos/248547/pexels-photo-248547.jpeg"),
    BASEBALL(
        87,
        "Baseball",
        "https://images.pexels.com/photos/163487/baseball-player-pitcher-ball-163487.jpeg"
    ),
    BASKETBALL(
        88,
        "Basketball",
        "https://images.pexels.com/photos/2834917/pexels-photo-2834917.jpeg"
    ),
    CRICKET(89, "Cricket", "https://images.pexels.com/photos/3657154/pexels-photo-3657154.jpeg"),
    FANTASY(90, "Fantasy", "https://images.pexels.com/photos/266429/pexels-photo-266429.jpeg"),
    FOOTBALL(91, "Football", "https://images.pexels.com/photos/1618200/pexels-photo-1618200.jpeg"),
    GOLF(92, "Golf", "https://images.pexels.com/photos/1409004/pexels-photo-1409004.jpeg"),
    HOCKEY(93, "Hockey", "https://images.pexels.com/photos/3159812/pexels-photo-3159812.jpeg"),
    RUGBY(94, "Rugby", "https://images.pexels.com/photos/3662553/pexels-photo-3662553.jpeg"),
    RUNNING(95, "Running", "https://images.pexels.com/photos/34514/spot-runs-start-la.jpg"),
    SOCCER(96, "Soccer", "https://images.pexels.com/photos/3148452/pexels-photo-3148452.jpeg"),
    SWIMMING(
        97,
        "Swimming",
        "https://images.pexels.com/photos/73760/swimming-swimmer-female-race-73760.jpeg"
    ),
    TENNIS(98, "Tennis", "https://images.pexels.com/photos/5409085/pexels-photo-5409085.jpeg"),
    VOLLEYBALL(
        99,
        "Volleyball",
        "https://images.pexels.com/photos/6180399/pexels-photo-6180399.jpeg"
    ),
    WILDERNESS(
        100,
        "Wilderness",
        "https://images.pexels.com/photos/134058/pexels-photo-134058.jpeg"
    ),
    WRESTLING(
        101,
        "Wrestling",
        "https://images.pexels.com/photos/8611375/pexels-photo-8611375.jpeg"
    ),
    TECHNOLOGY(
        102,
        "Technology",
        "https://images.pexels.com/photos/256381/pexels-photo-256381.jpeg"
    ),
    TRUE_CRIME(
        103,
        "True Crime",
        "https://images.pexels.com/photos/6065130/pexels-photo-6065130.jpeg"
    ),
    TV(104, "TV", "https://images.pexels.com/photos/3764958/pexels-photo-3764958.jpeg"),
    FILM(105, "Film", "https://images.pexels.com/photos/2524145/pexels-photo-2524145.jpeg"),
    AFTER_SHOWS(
        106,
        "After-Shows",
        "https://images.pexels.com/photos/713149/pexels-photo-713149.jpeg"
    ),
    REVIEWS(107, "Reviews", "https://images.pexels.com/photos/4065400/pexels-photo-4065400.jpeg"),
    CLIMATE(108, "Climate", "https://images.pexels.com/photos/1647220/pexels-photo-1647220.jpeg"),
    WEATHER(109, "Weather", "https://images.pexels.com/photos/1915182/pexels-photo-1915182.jpeg"),
    TABLETOP(110, "Tabletop", "https://images.pexels.com/photos/1684151/pexels-photo-1684151.jpeg"),
    ROLE_PLAYING(
        111,
        "Role-Playing",
        "https://images.pexels.com/photos/1049746/pexels-photo-1049746.jpeg"
    ),
    CRYPTOCURRENCY(
        112,
        "Cryptocurrency",
        "https://images.pexels.com/photos/5980916/pexels-photo-5980916.jpeg"
    ),
}
