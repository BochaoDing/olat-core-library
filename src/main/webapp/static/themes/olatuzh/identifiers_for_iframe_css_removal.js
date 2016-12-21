/*********************************************************************************
 WORAROUND to fix style issues of (broken) custom content
 @author: Christian Meier <christian.meier3@uzh.ch>
 @see: fix_style_issues.js
 @see: content_correction.css
 @see: theme_correction.css

 Place here identifiers of affected resources which have to be cleared up.
 Push a {key : value} pair with certain {"RepositoryEntry" : "CourseNode"} - or use "all" as CourseNode to handle all Elements within a Course

 MIGRATION of custom course styles:
 Move existing CSS file defining course-layout to a new folder "courseCSS" in storage folder and reference it via GUI at Course > Layout
 HINT: Usage of special CSS selectors for custom content is highly recommended to avoid interferences with global theme layout!

 CAVEAT:
 (1) If Course > Layout = Default to omit courseCSS, DO NOT INCLUDE these Courses below to preserve global theme layout for custom content!
 (2) For custom styles used in Content Packages (e.g. ELML based content) removal is suitable.
/*********************************************************************************/

// @see: OLATNG-155 to identify affected courses
var identifiers_for_iframe_css_removal = [];

/*********************************************************************************/
/* (1) Courses with custom Layout using courseCSS                                */
/*********************************************************************************/

// CareOL CBZ HF Pflege
// https://lms.uzh.ch/url/RepositoryEntry/627310604
identifiers_for_iframe_css_removal.push({"627310604" : "all"});

// CareOL CDH
// https://lms.uzh.ch/url/RepositoryEntry/1402961935
identifiers_for_iframe_css_removal.push({"1402961935" : "all"});

// CareOL CBZ MTR
// https://lms.uzh.ch/url/RepositoryEntry/725516293
identifiers_for_iframe_css_removal.push({"725516293" : "all"});

/* Begin of CareOL Course with Layout = Default */
/*
// CareOL Austausch
// https://lms.uzh.ch/url/RepositoryEntry/586612738
identifiers_for_iframe_css_removal.push({"586612738" : "all"});

// CareOL BGS Austausch
// https://lms.uzh.ch/url/RepositoryEntry/439353360
identifiers_for_iframe_css_removal.push({"439353360" : "all"});

// CareOL BGS Home
// https://lms.uzh.ch/url/RepositoryEntry/684097555
identifiers_for_iframe_css_removal.push({"684097555" : "all"});

// CareOL BZGS 10-OT
// https://lms.uzh.ch/url/RepositoryEntry/11280089094
identifiers_for_iframe_css_removal.push({"11280089094" : "all"});

// CareOL BZGS 10BMA
// https://lms.uzh.ch/url/RepositoryEntry/11280089096
identifiers_for_iframe_css_removal.push({"11280089096" : "all"});

// CareOL BZGS 11-OT
// https://lms.uzh.ch/url/RepositoryEntry/13617922063
identifiers_for_iframe_css_removal.push({"13617922063" : "all"});

// CareOL BZGS 11BMA
// https://lms.uzh.ch/url/RepositoryEntry/13663305730
identifiers_for_iframe_css_removal.push({"13663305730" : "all"});

// CareOL BZGS 8-OT
// https://lms.uzh.ch/url/RepositoryEntry/6185353217
identifiers_for_iframe_css_removal.push({"6185353217" : "all"});

// CareOL BZGS 8BMA
// https://lms.uzh.ch/url/RepositoryEntry/6185353216
identifiers_for_iframe_css_removal.push({"6185353216" : "all"});

// CareOL BZGS 9-OT
// https://lms.uzh.ch/url/RepositoryEntry/8749056007
identifiers_for_iframe_css_removal.push({"8749056007" : "all"});

// CareOL BZGS 9BMA
// https://lms.uzh.ch/url/RepositoryEntry/8749056008
identifiers_for_iframe_css_removal.push({"8749056008" : "all"});

// CareOL Demo
// https://lms.uzh.ch/url/RepositoryEntry/4403331073
identifiers_for_iframe_css_removal.push({"4403331073" : "all"});

// CareOL E-Books Test
// https://lms.uzh.ch/url/RepositoryEntry/12859572224
identifiers_for_iframe_css_removal.push({"12859572224" : "all"});

// CareOL Helga
// https://lms.uzh.ch/url/RepositoryEntry/283410454
identifiers_for_iframe_css_removal.push({"283410454" : "all"});

// CareOL Helga II
// https://lms.uzh.ch/url/RepositoryEntry/448659468
identifiers_for_iframe_css_removal.push({"448659468" : "all"});

// CareOL Literatur
// https://lms.uzh.ch/url/RepositoryEntry/12621742080
identifiers_for_iframe_css_removal.push({"12621742080" : "all"});

// Die Lernplattform "CareOL"
// https://lms.uzh.ch/url/RepositoryEntry/5046534146
identifiers_for_iframe_css_removal.push({"5046534146" : "all"});

// Schulung_CareOL_CBZ
// https://lms.uzh.ch/url/RepositoryEntry/3910336514
identifiers_for_iframe_css_removal.push({"3910336514" : "all"});
*/
/* End of CareOL Course with Layout = Default */

// Kurs PF F 14
// https://lms.uzh.ch/url/RepositoryEntry/9459433474
identifiers_for_iframe_css_removal.push({"9459433474" : "all"});

// FrS16_P04_Lernpsychologie II
// https://lms.uzh.ch/url/RepositoryEntry/14481752064
identifiers_for_iframe_css_removal.push({"14481752064" : "all"});

// Kurs PF F 16
// https://lms.uzh.ch/url/RepositoryEntry/14110949376
identifiers_for_iframe_css_removal.push({"14110949376" : "all"});

// Kurs PF F 15
// https://lms.uzh.ch/url/RepositoryEntry/11374755845
identifiers_for_iframe_css_removal.push({"11374755845" : "all"});

// Kurs DH 15
// https://lms.uzh.ch/url/RepositoryEntry/13347061760
identifiers_for_iframe_css_removal.push({"13347061760" : "all"});

// CareOL CBZ BMA
// https://lms.uzh.ch/url/RepositoryEntry/768835584
identifiers_for_iframe_css_removal.push({"768835584" : "all"});

// Latinum electronicum (fran√ßais)
// https://lms.uzh.ch/url/RepositoryEntry/952991788
identifiers_for_iframe_css_removal.push({"952991788" : "all"});

// Kurs PF F 13
// https://lms.uzh.ch/url/RepositoryEntry/7199588352
identifiers_for_iframe_css_removal.push({"7199588352" : "all"});

// Kurs OT H15
// https://lms.uzh.ch/url/RepositoryEntry/13602488320
identifiers_for_iframe_css_removal.push({"13602488320" : "all"});

// CareOL CBZ OT
// https://lms.uzh.ch/url/RepositoryEntry/12042698752
identifiers_for_iframe_css_removal.push({"12042698752" : "all"});

// FA H15/15
// https://lms.uzh.ch/url/RepositoryEntry/13299679249
identifiers_for_iframe_css_removal.push({"13299679249" : "all"});

// Kurs PF H 15
// https://lms.uzh.ch/url/RepositoryEntry/12728270848
identifiers_for_iframe_css_removal.push({"12728270848" : "all"});

// Begleitung P04 FrS 16
// https://lms.uzh.ch/url/RepositoryEntry/14855012352
identifiers_for_iframe_css_removal.push({"14855012352" : "all"});

// Kurs PF FA F 16
// https://lms.uzh.ch/url/RepositoryEntry/14110949379
identifiers_for_iframe_css_removal.push({"14110949379" : "all"});

// Kurs PF H 14
// https://lms.uzh.ch/url/RepositoryEntry/10417405955
identifiers_for_iframe_css_removal.push({"10417405955" : "all"});

// Microbial Diversity 2016
// https://lms.uzh.ch/url/RepositoryEntry/15765241857
identifiers_for_iframe_css_removal.push({"15765241857" : "all"});

// FA H15/3
// https://lms.uzh.ch/url/RepositoryEntry/13299679236
identifiers_for_iframe_css_removal.push({"13299679236" : "all"});

// BFS FAGE
// https://lms.uzh.ch/url/RepositoryEntry/7666466816
identifiers_for_iframe_css_removal.push({"7666466816" : "all"});

// eTutorial Accounting
// https://lms.uzh.ch/url/RepositoryEntry/2007498772
identifiers_for_iframe_css_removal.push({"2007498772" : "all"});

// FA H15/14
// https://lms.uzh.ch/url/RepositoryEntry/13299679248
identifiers_for_iframe_css_removal.push({"13299679248" : "all"});

// Kurs PF FA H 14
// https://lms.uzh.ch/url/RepositoryEntry/10417405953
identifiers_for_iframe_css_removal.push({"10417405953" : "all"});

// Kurs PF H 13
// https://lms.uzh.ch/url/RepositoryEntry/8171782149
identifiers_for_iframe_css_removal.push({"8171782149" : "all"});

// Latinum electronicum Fribourg (deutsch)
// https://lms.uzh.ch/url/RepositoryEntry/8507097088
identifiers_for_iframe_css_removal.push({"8507097088" : "all"});

// Kurs PF FA H 15
// https://lms.uzh.ch/url/RepositoryEntry/12596117505
identifiers_for_iframe_css_removal.push({"12596117505" : "all"});

// VAM-ZM Kieferorthop√§die und Kinderzahnmedizin
// https://lms.uzh.ch/url/RepositoryEntry/10290069505
identifiers_for_iframe_css_removal.push({"10290069505" : "all"});

// VAM-ZM Mund-, Kiefer- und Gesichtschirurgie, Oralchirurgie
// https://lms.uzh.ch/url/RepositoryEntry/13463650304
identifiers_for_iframe_css_removal.push({"13463650304" : "all"});

// VAM-ZM Kaufunktionsst√∂rungen abnehmbare Rekonstruktionen Alters- und Behindertenzahnmedizin
// https://lms.uzh.ch/url/RepositoryEntry/12242780160
identifiers_for_iframe_css_removal.push({"12242780160" : "all"});

// VAM-ZM Kronen- und Br√ºckenprothetik Teilprothetik und zahn√§rztliche Materialkunde
// https://lms.uzh.ch/url/RepositoryEntry/12238159874
identifiers_for_iframe_css_removal.push({"12238159874" : "all"});

// VAM-ZM Studieninfos 5. SJ
// https://lms.uzh.ch/url/RepositoryEntry/552009755
identifiers_for_iframe_css_removal.push({"552009755" : "all"});

// FA H15/12
// https://lms.uzh.ch/url/RepositoryEntry/13299679246
identifiers_for_iframe_css_removal.push({"13299679246" : "all"});

// 2. Lehrjahr BK BFS
// https://lms.uzh.ch/url/RepositoryEntry/11021320193
identifiers_for_iframe_css_removal.push({"11021320193" : "all"});

// 1. Lehrjahr Berufskunde BFS
// https://lms.uzh.ch/url/RepositoryEntry/7666466817
identifiers_for_iframe_css_removal.push({"7666466817" : "all"});

// VAM-ZM Pr√§ventivzahnmedizin Parodontologie und Kariologie
// https://lms.uzh.ch/url/RepositoryEntry/11116347396
identifiers_for_iframe_css_removal.push({"11116347396" : "all"});

// VAM-ZM Studieninfos 3. SJ
// https://lms.uzh.ch/url/RepositoryEntry/195985421
identifiers_for_iframe_css_removal.push({"195985421" : "all"});

// Kurs  HF  BMA H15
// https://lms.uzh.ch/url/RepositoryEntry/13605830658
identifiers_for_iframe_css_removal.push({"13605830658" : "all"});

// Kurs PF H 16
// https://lms.uzh.ch/url/RepositoryEntry/16036790282
identifiers_for_iframe_css_removal.push({"16036790282" : "all"});

// Kurs PF FA H 16
// https://lms.uzh.ch/auth/RepositoryEntry/16041312259
identifiers_for_iframe_css_removal.push({"16041312259" : "all"});

// Kurs HF BMA H16
// https://lms.uzh.ch/auth/RepositoryEntry/16110059554
identifiers_for_iframe_css_removal.push({"16110059554" : "all"});

// VAM-ZM Allgemein medizinische F√§cher
// https://lms.uzh.ch/url/RepositoryEntry/11116347392
identifiers_for_iframe_css_removal.push({"11116347392" : "all"});

// RGNO_Namibia_2016
// https://lms.uzh.ch/url/RepositoryEntry/14345109504
identifiers_for_iframe_css_removal.push({"14345109504" : "all"});

// Tr√§chtigkeit
// https://lms.uzh.ch/url/RepositoryEntry/1205829633
identifiers_for_iframe_css_removal.push({"1205829633" : "all"});

// VAM-ZM Studieninfos 4. SJ
// https://lms.uzh.ch/url/RepositoryEntry/421396484
identifiers_for_iframe_css_removal.push({"421396484" : "all"});

// Die Pferdeklinik
// https://lms.uzh.ch/url/RepositoryEntry/942243845
identifiers_for_iframe_css_removal.push({"942243845" : "all"});

// PSYCH_10_FS Themen der Psychologie
// https://lms.uzh.ch/url/RepositoryEntry/1679491072
identifiers_for_iframe_css_removal.push({"1679491072" : "all"});

// FA H14/10
// https://lms.uzh.ch/url/RepositoryEntry/11032461319
identifiers_for_iframe_css_removal.push({"11032461319" : "all"});

// 3. Lehrjahr Berufskunde BFS
// https://lms.uzh.ch/url/RepositoryEntry/12532776961
identifiers_for_iframe_css_removal.push({"12532776961" : "all"});

// Latinum electronicum - Summerschool
// https://lms.uzh.ch/url/RepositoryEntry/1261010948
identifiers_for_iframe_css_removal.push({"1261010948" : "all"});

// FA H14/5
// https://lms.uzh.ch/url/RepositoryEntry/11034853380
identifiers_for_iframe_css_removal.push({"11034853380" : "all"});

// FA H15/6
// https://lms.uzh.ch/url/RepositoryEntry/13299679240
identifiers_for_iframe_css_removal.push({"13299679240" : "all"});

// FA H15/7
// https://lms.uzh.ch/url/RepositoryEntry/13299679241
identifiers_for_iframe_css_removal.push({"13299679241" : "all"});

// FA H14/9
// https://lms.uzh.ch/url/RepositoryEntry/11032461318
identifiers_for_iframe_css_removal.push({"11032461318" : "all"});

// VAM-ZM Orale Biologie
// https://lms.uzh.ch/url/RepositoryEntry/13463650305
identifiers_for_iframe_css_removal.push({"13463650305" : "all"});

// FA H15/2
// https://lms.uzh.ch/url/RepositoryEntry/13299679234
identifiers_for_iframe_css_removal.push({"13299679234" : "all"});

// Kurs H15
// https://lms.uzh.ch/url/RepositoryEntry/13569130496
identifiers_for_iframe_css_removal.push({"13569130496" : "all"});

// Kurs PF FA H 16
// https://lms.uzh.ch/url/RepositoryEntry/15906766849
identifiers_for_iframe_css_removal.push({"15906766849" : "all"});

// Kurs PF BB  H 16
// https://lms.uzh.ch/url/RepositoryEntry/16016572424
identifiers_for_iframe_css_removal.push({"16016572424" : "all"});

// Kurs PF H 16
// https://lms.uzh.ch/url/RepositoryEntry/15906766848
identifiers_for_iframe_css_removal.push({"15906766848" : "all"});

// Vorlage_Kurs_PF_F/H_XX
// https://lms.uzh.ch/url/RepositoryEntry/15209562112
identifiers_for_iframe_css_removal.push({"15209562112" : "all"});

// Vorlage_Kurs_PF_FA_XXx
// https://lms.uzh.ch/url/RepositoryEntry/15209562113
identifiers_for_iframe_css_removal.push({"15209562113" : "all"});

// Test Kurs H16
// https://lms.uzh.ch/url/RepositoryEntry/15763013632
identifiers_for_iframe_css_removal.push({"15763013632" : "all"});

// FA H14/7
// https://lms.uzh.ch/url/RepositoryEntry/11034853382
identifiers_for_iframe_css_removal.push({"11034853382" : "all"});

// Financial Markets
// https://lms.uzh.ch/url/RepositoryEntry/122028035
identifiers_for_iframe_css_removal.push({"122028035" : "all"});

// FA H15/10
// https://lms.uzh.ch/url/RepositoryEntry/13299679244
identifiers_for_iframe_css_removal.push({"13299679244" : "all"});

// Gi - Gespr√§chsanalyse interaktiv 2016
// https://lms.uzh.ch/url/RepositoryEntry/14303854592
identifiers_for_iframe_css_removal.push({"14303854592" : "all"});

// Kurs DH H16
// https://lms.uzh.ch/url/RepositoryEntry/12887162881
identifiers_for_iframe_css_removal.push({"12887162881" : "all"});

// Latinum electronicum - Demo-Kurs
// https://lms.uzh.ch/url/RepositoryEntry/1018986511
identifiers_for_iframe_css_removal.push({"1018986511" : "all"});

// Latinum electronicum
// https://lms.uzh.ch/url/RepositoryEntry/134512641
identifiers_for_iframe_css_removal.push({"134512641" : "all"});

// FA H14/6
// https://lms.uzh.ch/url/RepositoryEntry/11034853381
identifiers_for_iframe_css_removal.push({"11034853381" : "all"});

// FA H15/4
// https://lms.uzh.ch/url/RepositoryEntry/13299679238
identifiers_for_iframe_css_removal.push({"13299679238" : "all"});

// FA H14/3
// https://lms.uzh.ch/url/RepositoryEntry/11034853378
identifiers_for_iframe_css_removal.push({"11034853378" : "all"});

// FA H14/4
// https://lms.uzh.ch/url/RepositoryEntry/11034853379
identifiers_for_iframe_css_removal.push({"11034853379" : "all"});

// FA H15/8
// https://lms.uzh.ch/url/RepositoryEntry/13299679242
identifiers_for_iframe_css_removal.push({"13299679242" : "all"});

// Geburt
// https://lms.uzh.ch/url/RepositoryEntry/1394311174
identifiers_for_iframe_css_removal.push({"1394311174" : "all"});

// Kurs PF FA F 15
// https://lms.uzh.ch/url/RepositoryEntry/11374755843
identifiers_for_iframe_css_removal.push({"11374755843" : "all"});

// FA H14/13
// https://lms.uzh.ch/url/RepositoryEntry/11032461322
identifiers_for_iframe_css_removal.push({"11032461322" : "all"});

// HeS16 Umgang mit Heterogenit√§t
// https://lms.uzh.ch/url/RepositoryEntry/15780904962
identifiers_for_iframe_css_removal.push({"15780904962" : "all"});

// FA H15/13
// https://lms.uzh.ch/url/RepositoryEntry/13299679247
identifiers_for_iframe_css_removal.push({"13299679247" : "all"});

// FA H14/11
// https://lms.uzh.ch/url/RepositoryEntry/11032461320
identifiers_for_iframe_css_removal.push({"11032461320" : "all"});

// Fallpr√§sentationen_Pferdechirurgie
// https://lms.uzh.ch/url/RepositoryEntry/955514880
identifiers_for_iframe_css_removal.push({"955514880" : "all"});

// FA H14/8
// https://lms.uzh.ch/url/RepositoryEntry/11032461316
identifiers_for_iframe_css_removal.push({"11032461316" : "all"});

// FA H15/1
// https://lms.uzh.ch/url/RepositoryEntry/13299679233
identifiers_for_iframe_css_removal.push({"13299679233" : "all"});

// eVet Respi
// https://lms.uzh.ch/url/RepositoryEntry/953778177
identifiers_for_iframe_css_removal.push({"953778177" : "all"});

// gi - light 2012
// https://lms.uzh.ch/url/RepositoryEntry/5557813253
identifiers_for_iframe_css_removal.push({"5557813253" : "all"});

// FA H15/16
// https://lms.uzh.ch/url/RepositoryEntry/15940091906
identifiers_for_iframe_css_removal.push({"15940091906" : "all"});

// FA H14/1
// https://lms.uzh.ch/url/RepositoryEntry/11020566528
identifiers_for_iframe_css_removal.push({"11020566528" : "all"});

// FA H15/5
// https://lms.uzh.ch/url/RepositoryEntry/13299679239
identifiers_for_iframe_css_removal.push({"13299679239" : "all"});

// FA H16/1
// https://lms.uzh.ch/url/RepositoryEntry/15757410305
identifiers_for_iframe_css_removal.push({"15757410305" : "all"});

// 16FS 164d/168d/556d/559dm2 Einf√ºhrung in die Gespr√§chslinguistik
// https://lms.uzh.ch/url/RepositoryEntry/14859403269
identifiers_for_iframe_css_removal.push({"14859403269" : "all"});

// FA H15/11
// https://lms.uzh.ch/url/RepositoryEntry/13299679245
identifiers_for_iframe_css_removal.push({"13299679245" : "all"});

// FA H15/9
// https://lms.uzh.ch/url/RepositoryEntry/13299679243
identifiers_for_iframe_css_removal.push({"13299679243" : "all"});

// FA H14/14
// https://lms.uzh.ch/url/RepositoryEntry/11032461323
identifiers_for_iframe_css_removal.push({"11032461323" : "all"});

// FA H14/12
// https://lms.uzh.ch/url/RepositoryEntry/11032461321
identifiers_for_iframe_css_removal.push({"11032461321" : "all"});

// FA H14/2
// https://lms.uzh.ch/url/RepositoryEntry/11034853377
identifiers_for_iframe_css_removal.push({"11034853377" : "all"});

// ECODIM_2016
// https://lms.uzh.ch/url/RepositoryEntry/14413791232
identifiers_for_iframe_css_removal.push({"14413791232" : "all"});

// 16FS Introduction to Financial Economics (L)
// https://lms.uzh.ch/url/RepositoryEntry/14859403264
identifiers_for_iframe_css_removal.push({"14859403264" : "all"});

// HfH_Lernpsychologie-Lernen verstehen
// https://lms.uzh.ch/url/RepositoryEntry/12934742016
identifiers_for_iframe_css_removal.push({"12934742016" : "all"});

// Lernwelt Lernen lernen
// https://lms.uzh.ch/url/RepositoryEntry/7666466818
identifiers_for_iframe_css_removal.push({"7666466818" : "all"});

// 1. Lehrjahr BFS ABU
// https://lms.uzh.ch/url/RepositoryEntry/9440559104
identifiers_for_iframe_css_removal.push({"9440559104" : "all"});

// IBF FS14 - Banking (Birchler W√ºnsch  Lautenschlager) - 370 (BOEC319)
// https://lms.uzh.ch/url/RepositoryEntry/8845033481
identifiers_for_iframe_css_removal.push({"8845033481" : "all"});

// EGONE Zuerich
// https://lms.uzh.ch/url/RepositoryEntry/24313859
identifiers_for_iframe_css_removal.push({"24313859" : "all"});

// TUSTEP-Tutorial
// https://lms.uzh.ch/url/RepositoryEntry/957513742
identifiers_for_iframe_css_removal.push({"957513742" : "all"});

// FA H13/1
// https://lms.uzh.ch/url/RepositoryEntry/8477507587
identifiers_for_iframe_css_removal.push({"8477507587" : "all"});

// FA H13/8
// https://lms.uzh.ch/url/RepositoryEntry/8491925508
identifiers_for_iframe_css_removal.push({"8491925508" : "all"});

// FA H16/9
// https://lms.uzh.ch/url/RepositoryEntry/15760064520
identifiers_for_iframe_css_removal.push({"15760064520" : "all"});

// Vorlage Klasse FA HXX/X
// https://lms.uzh.ch/url/RepositoryEntry/8261009408
identifiers_for_iframe_css_removal.push({"8261009408" : "all"});

// Corporate Finance I - eCF Basic - HS11
// https://lms.uzh.ch/url/RepositoryEntry/3923935232
identifiers_for_iframe_css_removal.push({"3923935232" : "all"});

// Kurs PF FA F 14
// https://lms.uzh.ch/url/RepositoryEntry/9459433472
identifiers_for_iframe_css_removal.push({"9459433472" : "all"});

// FA H13/2
// https://lms.uzh.ch/url/RepositoryEntry/8477114369
identifiers_for_iframe_css_removal.push({"8477114369" : "all"});

// FrS15_Lernpsychologie II (P04)
// https://lms.uzh.ch/url/RepositoryEntry/11963564033
identifiers_for_iframe_css_removal.push({"11963564033" : "all"});

// Brancheninformation
// https://lms.uzh.ch/url/RepositoryEntry/8389165056
identifiers_for_iframe_css_removal.push({"8389165056" : "all"});

// FA H13/4
// https://lms.uzh.ch/url/RepositoryEntry/8491925504
identifiers_for_iframe_css_removal.push({"8491925504" : "all"});

// Geomicrobiology_16
// https://lms.uzh.ch/url/RepositoryEntry/14217969664
identifiers_for_iframe_css_removal.push({"14217969664" : "all"});

// Kurs HF BMA
// https://lms.uzh.ch/url/RepositoryEntry/13600129029
identifiers_for_iframe_css_removal.push({"13600129029" : "all"});

// GONE Zuerich
// https://lms.uzh.ch/url/RepositoryEntry/211582990
identifiers_for_iframe_css_removal.push({"211582990" : "all"});

// Kurs H16
// https://lms.uzh.ch/url/RepositoryEntry/15725625344
identifiers_for_iframe_css_removal.push({"15725625344" : "all"});

// FA H16/8
// https://lms.uzh.ch/url/RepositoryEntry/15760064519
identifiers_for_iframe_css_removal.push({"15760064519" : "all"});

// Master gi - light
// https://lms.uzh.ch/url/RepositoryEntry/6241386499
identifiers_for_iframe_css_removal.push({"6241386499" : "all"});

// Onlinepr√ºfung FS16 PZV
// https://lms.uzh.ch/url/RepositoryEntry/15763374089
identifiers_for_iframe_css_removal.push({"15763374089" : "all"});

// Dermatologie
// https://lms.uzh.ch/url/RepositoryEntry/1923612674
identifiers_for_iframe_css_removal.push({"1923612674" : "all"});

// FA H13/9
// https://lms.uzh.ch/url/RepositoryEntry/8491925509
identifiers_for_iframe_css_removal.push({"8491925509" : "all"});

// 2. Lehrjahr ABU BFS
// https://lms.uzh.ch/url/RepositoryEntry/11033378878
identifiers_for_iframe_css_removal.push({"11033378878" : "all"});

// Vorlage Kurs BMA
// https://lms.uzh.ch/url/RepositoryEntry/12887162883
identifiers_for_iframe_css_removal.push({"12887162883" : "all"});

// gi - Basis 2014
// https://lms.uzh.ch/url/RepositoryEntry/10322018305
identifiers_for_iframe_css_removal.push({"10322018305" : "all"});

// Gi - Gespr√§chsanalyse interaktiv 2015
// https://lms.uzh.ch/url/RepositoryEntry/11887869954
identifiers_for_iframe_css_removal.push({"11887869954" : "all"});

// gi-light - Seminar: Empraktische Kommunikation
// https://lms.uzh.ch/url/RepositoryEntry/13974568962
identifiers_for_iframe_css_removal.push({"13974568962" : "all"});

// CareOL HF Pflege BB
// https://lms.uzh.ch/url/RepositoryEntry/14827847688
identifiers_for_iframe_css_removal.push({"14827847688" : "all"});

// FA H13/14
// https://lms.uzh.ch/url/RepositoryEntry/8495169540
identifiers_for_iframe_css_removal.push({"8495169540" : "all"});

// FA H13/10
// https://lms.uzh.ch/url/RepositoryEntry/8491925510
identifiers_for_iframe_css_removal.push({"8491925510" : "all"});

// Vorlage Kurs HF MTR
// https://lms.uzh.ch/url/RepositoryEntry/15823142912
identifiers_for_iframe_css_removal.push({"15823142912" : "all"});

// Kurs OT H16
// https://lms.uzh.ch/url/RepositoryEntry/15822684160
identifiers_for_iframe_css_removal.push({"15822684160" : "all"});

// Vorlage Kurs OT
// https://lms.uzh.ch/url/RepositoryEntry/12887162882
identifiers_for_iframe_css_removal.push({"12887162882" : "all"});

// FA H16/17
// https://lms.uzh.ch/url/RepositoryEntry/15760064528
identifiers_for_iframe_css_removal.push({"15760064528" : "all"});

// FA H13/3
// https://lms.uzh.ch/url/RepositoryEntry/8477114370
identifiers_for_iframe_css_removal.push({"8477114370" : "all"});

// IBF FS13 - Banking (Birchler W√ºnsch Lautenschlager) - 334 (BOEC319)
// https://lms.uzh.ch/url/RepositoryEntry/6390710274
identifiers_for_iframe_css_removal.push({"6390710274" : "all"});

// Financial Markets (Demo Course)
// https://lms.uzh.ch/url/RepositoryEntry/1985019913
identifiers_for_iframe_css_removal.push({"1985019913" : "all"});

// Vorlage Kurs DH
// https://lms.uzh.ch/url/RepositoryEntry/15784869888
identifiers_for_iframe_css_removal.push({"15784869888" : "all"});

// HeS15 Umgang mit Heterogenit√§t
// https://lms.uzh.ch/url/RepositoryEntry/12867174400
identifiers_for_iframe_css_removal.push({"12867174400" : "all"});

// Financial Markets Z√ºrich
// https://lms.uzh.ch/url/RepositoryEntry/14446788611
identifiers_for_iframe_css_removal.push({"14446788611" : "all"});

// FA H13/11
// https://lms.uzh.ch/url/RepositoryEntry/8495169537
identifiers_for_iframe_css_removal.push({"8495169537" : "all"});

// Latinum electronicum Fribourg (fran√ßais)
// https://lms.uzh.ch/url/RepositoryEntry/8388542464
identifiers_for_iframe_css_removal.push({"8388542464" : "all"});

// Latinum electronicum Lausanne
// https://lms.uzh.ch/url/RepositoryEntry/2615574530
identifiers_for_iframe_css_removal.push({"2615574530" : "all"});

// FA H16/16
// https://lms.uzh.ch/url/RepositoryEntry/15760064527
identifiers_for_iframe_css_removal.push({"15760064527" : "all"});

// FA H16/15
// https://lms.uzh.ch/url/RepositoryEntry/15760064526
identifiers_for_iframe_css_removal.push({"15760064526" : "all"});

// FA H16/14
// https://lms.uzh.ch/url/RepositoryEntry/15760064525
identifiers_for_iframe_css_removal.push({"15760064525" : "all"});

// FA H16/13
// https://lms.uzh.ch/url/RepositoryEntry/15760064524
identifiers_for_iframe_css_removal.push({"15760064524" : "all"});

// FA H16/12
// https://lms.uzh.ch/url/RepositoryEntry/15760064523
identifiers_for_iframe_css_removal.push({"15760064523" : "all"});

// FA H16/11
// https://lms.uzh.ch/url/RepositoryEntry/15760064522
identifiers_for_iframe_css_removal.push({"15760064522" : "all"});

// FA H16/10
// https://lms.uzh.ch/url/RepositoryEntry/15760064521
identifiers_for_iframe_css_removal.push({"15760064521" : "all"});

// FA H16/7
// https://lms.uzh.ch/url/RepositoryEntry/15760064517
identifiers_for_iframe_css_removal.push({"15760064517" : "all"});

// FA H16/6
// https://lms.uzh.ch/url/RepositoryEntry/15760064518
identifiers_for_iframe_css_removal.push({"15760064518" : "all"});

// FA H16/5
// https://lms.uzh.ch/url/RepositoryEntry/15760064516
identifiers_for_iframe_css_removal.push({"15760064516" : "all"});

// FA H16/4
// https://lms.uzh.ch/url/RepositoryEntry/15760064515
identifiers_for_iframe_css_removal.push({"15760064515" : "all"});

// FA H16/3
// https://lms.uzh.ch/url/RepositoryEntry/15760064513
identifiers_for_iframe_css_removal.push({"15760064513" : "all"});

// FA H16/2
// https://lms.uzh.ch/url/RepositoryEntry/15760064512
identifiers_for_iframe_css_removal.push({"15760064512" : "all"});

// Onlinepr√ºfung HS14 PZV
// https://lms.uzh.ch/url/RepositoryEntry/12150341632
identifiers_for_iframe_css_removal.push({"12150341632" : "all"});

// Onlinepr√ºfung HS15
// https://lms.uzh.ch/url/RepositoryEntry/14619017220
identifiers_for_iframe_css_removal.push({"14619017220" : "all"});

// FA H13/12
// https://lms.uzh.ch/url/RepositoryEntry/8495169538
identifiers_for_iframe_css_removal.push({"8495169538" : "all"});

// Master gi - Vollkurs
// https://lms.uzh.ch/url/RepositoryEntry/6095405061
identifiers_for_iframe_css_removal.push({"6095405061" : "all"});

// NEU Master gi - Vollkurs
// https://lms.uzh.ch/url/RepositoryEntry/15714648069
identifiers_for_iframe_css_removal.push({"15714648069" : "all"});

// FA H13/13
// https://lms.uzh.ch/url/RepositoryEntry/8495169539
identifiers_for_iframe_css_removal.push({"8495169539" : "all"});

// FA H13/7
// https://lms.uzh.ch/url/RepositoryEntry/8491925507
identifiers_for_iframe_css_removal.push({"8491925507" : "all"});

// FA H13/6
// https://lms.uzh.ch/url/RepositoryEntry/8491925506
identifiers_for_iframe_css_removal.push({"8491925506" : "all"});

// FA H13/5
// https://lms.uzh.ch/url/RepositoryEntry/8491925505
identifiers_for_iframe_css_removal.push({"8491925505" : "all"});

// Onlinepr√ºfung HS15 - TEST
// https://lms.uzh.ch/url/RepositoryEntry/14399569921
identifiers_for_iframe_css_removal.push({"14399569921" : "all"});

// Early English Online
// https://lms.uzh.ch/url/RepositoryEntry/173506564
identifiers_for_iframe_css_removal.push({"173506564" : "all"});

// VAM-ZM Sandbox
// https://lms.uzh.ch/url/RepositoryEntry/9346777091
identifiers_for_iframe_css_removal.push({"9346777091" : "all"});

// gi - light 2011
// https://lms.uzh.ch/url/RepositoryEntry/3933044736
identifiers_for_iframe_css_removal.push({"3933044736" : "all"});

// BLAW Online-Pr√ºfung
// https://lms.uzh.ch/url/RepositoryEntry/8394702856
identifiers_for_iframe_css_removal.push({"8394702856" : "all"});

// Geobiology_16
// https://lms.uzh.ch/url/RepositoryEntry/15294070784
identifiers_for_iframe_css_removal.push({"15294070784" : "all"});

// Onlinepr√ºfung Registration
// https://lms.uzh.ch/url/RepositoryEntry/15598256129
identifiers_for_iframe_css_removal.push({"15598256129" : "all"});

// Onlinepr√ºfung Test
// https://lms.uzh.ch/url/RepositoryEntry/15598256130
identifiers_for_iframe_css_removal.push({"15598256130" : "all"});

// _TEST Onlinepr√ºfung FS14
// https://lms.uzh.ch/url/RepositoryEntry/10769203203
identifiers_for_iframe_css_removal.push({"10769203203" : "all"});

// RGNO_Namibia_2015
// https://lms.uzh.ch/url/RepositoryEntry/11651416065
identifiers_for_iframe_css_removal.push({"11651416065" : "all"});

// Theologen-Latein
// https://lms.uzh.ch/url/RepositoryEntry/2053767172
identifiers_for_iframe_css_removal.push({"2053767172" : "all"});

// gi - light 2012 intern
// https://lms.uzh.ch/url/RepositoryEntry/4959797259
identifiers_for_iframe_css_removal.push({"4959797259" : "all"});

// Gespr√§chsanalyse interaktiv 2012
// https://lms.uzh.ch/url/RepositoryEntry/4825513988
identifiers_for_iframe_css_removal.push({"4825513988" : "all"});

// HF DH 2. Jahr
// https://lms.uzh.ch/url/RepositoryEntry/2706505728
identifiers_for_iframe_css_removal.push({"2706505728" : "all"});

// Gi - Gespr√§chsanalyse interaktiv 2013
// https://lms.uzh.ch/url/RepositoryEntry/7305494547
identifiers_for_iframe_css_removal.push({"7305494547" : "all"});

// Bongo
// https://lms.uzh.ch/url/RepositoryEntry/167575554
identifiers_for_iframe_css_removal.push({"167575554" : "all"});

// 1. Lehrjahr/2. Semester Berufskunde BFS
// https://lms.uzh.ch/url/RepositoryEntry/9766240256
identifiers_for_iframe_css_removal.push({"9766240256" : "all"});

// Webclass Basiskurs FS2008
// https://lms.uzh.ch/url/RepositoryEntry/793968658
identifiers_for_iframe_css_removal.push({"793968658" : "all"});

// Wirtschaftswissenschaften f√ºr Juristen - Kurs - HS08
// https://lms.uzh.ch/url/RepositoryEntry/932708378
identifiers_for_iframe_css_removal.push({"932708378" : "all"});

// RGNO_Namibia_2014
// https://lms.uzh.ch/url/RepositoryEntry/9474899968
identifiers_for_iframe_css_removal.push({"9474899968" : "all"});

// BIO146 2008SS Biochemistry and Physiology of Microorganisms
// https://lms.uzh.ch/url/RepositoryEntry/809009155
identifiers_for_iframe_css_removal.push({"809009155" : "all"});

// GEO 706 Ecology UZH 2010
// https://lms.uzh.ch/url/RepositoryEntry/1963425807
identifiers_for_iframe_css_removal.push({"1963425807" : "all"});

// Geomicrobiology_12
// https://lms.uzh.ch/url/RepositoryEntry/4977524736
identifiers_for_iframe_css_removal.push({"4977524736" : "all"});

// Geomicrobiology_11
// https://lms.uzh.ch/url/RepositoryEntry/3023503363
identifiers_for_iframe_css_removal.push({"3023503363" : "all"});

// BIO146 2006 Course Biochemistry and Physiology of Microorganisms
// https://lms.uzh.ch/url/RepositoryEntry/313032708
identifiers_for_iframe_css_removal.push({"313032708" : "all"});

// Geomicrobiology_10
// https://lms.uzh.ch/url/RepositoryEntry/1921024011
identifiers_for_iframe_css_removal.push({"1921024011" : "all"});

// gi - Basis 2013
// https://lms.uzh.ch/url/RepositoryEntry/5582585857
identifiers_for_iframe_css_removal.push({"5582585857" : "all"});

// gi - Vollkurs Bayreuth
// https://lms.uzh.ch/url/RepositoryEntry/9093447680
identifiers_for_iframe_css_removal.push({"9093447680" : "all"});

// GI
// https://lms.uzh.ch/url/RepositoryEntry/1232994304
identifiers_for_iframe_css_removal.push({"1232994304" : "all"});

// Geomicrobiology_15
// https://lms.uzh.ch/url/RepositoryEntry/12011569152
identifiers_for_iframe_css_removal.push({"12011569152" : "all"});

// ECVS exam TEST
// https://lms.uzh.ch/url/RepositoryEntry/1916534795
identifiers_for_iframe_css_removal.push({"1916534795" : "all"});

// TestKurs
// https://lms.uzh.ch/url/RepositoryEntry/9765453825
identifiers_for_iframe_css_removal.push({"9765453825" : "all"});

// TestFarbenBewertung
// https://lms.uzh.ch/url/RepositoryEntry/9784852481
identifiers_for_iframe_css_removal.push({"9784852481" : "all"});

// Latinum electronicum (cours d√©mo)
// https://lms.uzh.ch/url/RepositoryEntry/2181103616
identifiers_for_iframe_css_removal.push({"2181103616" : "all"});

// Latinum electronicum Oberwil
// https://lms.uzh.ch/url/RepositoryEntry/993591296
identifiers_for_iframe_css_removal.push({"993591296" : "all"});

// Latinum electronicum (Nissille)
// https://lms.uzh.ch/url/RepositoryEntry/962002988
identifiers_for_iframe_css_removal.push({"962002988" : "all"});

// IPK_AK_06/07_WS Einf√ºhrung_Alltagskulturanalyse
// https://lms.uzh.ch/url/RepositoryEntry/430342145
identifiers_for_iframe_css_removal.push({"430342145" : "all"});

// eTutorial Accounting
// https://lms.uzh.ch/url/RepositoryEntry/3177381888
identifiers_for_iframe_css_removal.push({"3177381888" : "all"});

// EZINEMA
// https://lms.uzh.ch/url/RepositoryEntry/12280561665
identifiers_for_iframe_css_removal.push({"12280561665" : "all"});

// Wirtschaftswissenschaften f√ºr Juristen - Kurs - HS09
// https://lms.uzh.ch/url/RepositoryEntry/1369866246
identifiers_for_iframe_css_removal.push({"1369866246" : "all"});

// VAM-ZM-3SJ-AllgemeinePathologie
// https://lms.uzh.ch/url/RepositoryEntry/1568702464
identifiers_for_iframe_css_removal.push({"1568702464" : "all"});

// Introduction to Financial Economics
// https://lms.uzh.ch/url/RepositoryEntry/14573797376
identifiers_for_iframe_css_removal.push({"14573797376" : "all"});

// Testvariante 1. Lehrjahr BK BFS
// https://lms.uzh.ch/url/RepositoryEntry/14805958668
identifiers_for_iframe_css_removal.push({"14805958668" : "all"});

// Testvariante BFS FAGE (Luka)
// https://lms.uzh.ch/url/RepositoryEntry/14805958667
identifiers_for_iframe_css_removal.push({"14805958667" : "all"});

// Testvorlage Klasse FA HXX/X
// https://lms.uzh.ch/url/RepositoryEntry/14805958666
identifiers_for_iframe_css_removal.push({"14805958666" : "all"});

// ECVS exam 2016
// https://lms.uzh.ch/url/RepositoryEntry/14647099392
identifiers_for_iframe_css_removal.push({"14647099392" : "all"});

// ECVS 2010 Demo
// https://lms.uzh.ch/url/RepositoryEntry/1476821008
identifiers_for_iframe_css_removal.push({"1476821008" : "all"});

// Corporate Finance I f√ºr FHNW
// https://lms.uzh.ch/url/RepositoryEntry/1215954956
identifiers_for_iframe_css_removal.push({"1215954956" : "all"});

// Onlinepr√ºfung HS13: Besondere Umst√§nde
// https://lms.uzh.ch/url/RepositoryEntry/9694412803
identifiers_for_iframe_css_removal.push({"9694412803" : "all"});

// ECVS exam
// https://lms.uzh.ch/url/RepositoryEntry/1888124934
identifiers_for_iframe_css_removal.push({"1888124934" : "all"});

// _TEST Onlinepr√ºfung HS14
// https://lms.uzh.ch/url/RepositoryEntry/12055314434
identifiers_for_iframe_css_removal.push({"12055314434" : "all"});

// 000_EinfRW_Serien_HS15
// https://lms.uzh.ch/url/RepositoryEntry/14472478720
identifiers_for_iframe_css_removal.push({"14472478720" : "all"});

// Latinum electronicum Bern
// https://lms.uzh.ch/url/RepositoryEntry/977010689
identifiers_for_iframe_css_removal.push({"977010689" : "all"});

// ECODIM_2014
// https://lms.uzh.ch/url/RepositoryEntry/9151610880
identifiers_for_iframe_css_removal.push({"9151610880" : "all"});

// ECODIM_2012
// https://lms.uzh.ch/url/RepositoryEntry/4773249024
identifiers_for_iframe_css_removal.push({"4773249024" : "all"});

// Onlinepr√ºfung-Testkurs FS15
// https://lms.uzh.ch/url/RepositoryEntry/13179944961
identifiers_for_iframe_css_removal.push({"13179944961" : "all"});

// Onlinepr√ºfung HS13: Besondere Pr√ºfungsbedingungen
// https://lms.uzh.ch/url/RepositoryEntry/9694412801
identifiers_for_iframe_css_removal.push({"9694412801" : "all"});

// Corporate Finance I - eCF Basic - HS09
// https://lms.uzh.ch/url/RepositoryEntry/1156382722
identifiers_for_iframe_css_removal.push({"1156382722" : "all"});

// BIO132_06 Mikrobiologie-Teil
// https://lms.uzh.ch/url/RepositoryEntry/422281221
identifiers_for_iframe_css_removal.push({"422281221" : "all"});

// Gespr√§chsanalyse interaktiv 2011
// https://lms.uzh.ch/url/RepositoryEntry/3243376646
identifiers_for_iframe_css_removal.push({"3243376646" : "all"});

// Gespr√§chsanalyse interaktiv 2010
// https://lms.uzh.ch/url/RepositoryEntry/1985019912
identifiers_for_iframe_css_removal.push({"1985019912" : "all"});

// Gi - Gespr√§chsanalyse interaktiv 2014
// https://lms.uzh.ch/url/RepositoryEntry/9780101123
identifiers_for_iframe_css_removal.push({"9780101123" : "all"});

// Geomicrobiology_13
// https://lms.uzh.ch/url/RepositoryEntry/7388594178
identifiers_for_iframe_css_removal.push({"7388594178" : "all"});

// 1. Lehrjahr/1. Semester ABU BFS
// https://lms.uzh.ch/url/RepositoryEntry/8231518209
identifiers_for_iframe_css_removal.push({"8231518209" : "all"});

// ROSE_12_FS 231 Dante: ¬´Purgatorio¬ª
// https://lms.uzh.ch/url/RepositoryEntry/5002657796
identifiers_for_iframe_css_removal.push({"5002657796" : "all"});

// Corporate Finance I - eCF Basic - HS10
// https://lms.uzh.ch/url/RepositoryEntry/2395602944
identifiers_for_iframe_css_removal.push({"2395602944" : "all"});

// Geomicrobiology_09
// https://lms.uzh.ch/url/RepositoryEntry/1083375635
identifiers_for_iframe_css_removal.push({"1083375635" : "all"});

// Test
// https://lms.uzh.ch/url/RepositoryEntry/8385331200
identifiers_for_iframe_css_removal.push({"8385331200" : "all"});

// IPK_AK_07_HS Einf√ºhrung_Alltagskulturanalyse
// https://lms.uzh.ch/url/RepositoryEntry/652279810
identifiers_for_iframe_css_removal.push({"652279810" : "all"});

// ECVDI exam
// https://lms.uzh.ch/url/RepositoryEntry/11160420353
identifiers_for_iframe_css_removal.push({"11160420353" : "all"});

// BIO126 2007HS Diversity of Microorganisms
// https://lms.uzh.ch/url/RepositoryEntry/656637970
identifiers_for_iframe_css_removal.push({"656637970" : "all"});

// Gespr√§chsanalyse interaktiv light
// https://lms.uzh.ch/url/RepositoryEntry/2298740737
identifiers_for_iframe_css_removal.push({"2298740737" : "all"});

// Kurs
// https://lms.uzh.ch/url/RepositoryEntry/8013217793
identifiers_for_iframe_css_removal.push({"8013217793" : "all"});

// Corporate Finance I - eCF Basic - HS08
// https://lms.uzh.ch/url/RepositoryEntry/894861315
identifiers_for_iframe_css_removal.push({"894861315" : "all"});

// Referenzversion Gespr√§chsanalyse interaktiv
// https://lms.uzh.ch/url/RepositoryEntry/2309488641
identifiers_for_iframe_css_removal.push({"2309488641" : "all"});

// gi Testversion Online-Lernen
// https://lms.uzh.ch/url/RepositoryEntry/6537510916
identifiers_for_iframe_css_removal.push({"6537510916" : "all"});

// Probepr√ºfung HS14
// https://lms.uzh.ch/url/RepositoryEntry/11949244428
identifiers_for_iframe_css_removal.push({"11949244428" : "all"});

// Master gi - Basis
// https://lms.uzh.ch/url/RepositoryEntry/6241386498
identifiers_for_iframe_css_removal.push({"6241386498" : "all"});

// Probepr√ºfung 2014
// https://lms.uzh.ch/url/RepositoryEntry/8297545734
identifiers_for_iframe_css_removal.push({"8297545734" : "all"});

// Geomicrobiology_14
// https://lms.uzh.ch/url/RepositoryEntry/9946267651
identifiers_for_iframe_css_removal.push({"9946267651" : "all"});

// Copy of FrS15_Lernpsychologie II (P04)
// https://lms.uzh.ch/url/RepositoryEntry/12548276224
identifiers_for_iframe_css_removal.push({"12548276224" : "all"});

// pps UniLA
// https://lms.uzh.ch/url/RepositoryEntry/1053491219
identifiers_for_iframe_css_removal.push({"1053491219" : "all"});

// pps ¬´processus politiques / suisse¬ª (UniGE)
// https://lms.uzh.ch/url/RepositoryEntry/562233385
identifiers_for_iframe_css_removal.push({"562233385" : "all"});

// IPK_HS08_Einf√ºhrung_Alltagskulturanalyse
// https://lms.uzh.ch/url/RepositoryEntry/825917455
identifiers_for_iframe_css_removal.push({"825917455" : "all"});

// UNAM Chemical and biological Oceanography 2011
// https://lms.uzh.ch/url/RepositoryEntry/4229365760
identifiers_for_iframe_css_removal.push({"4229365760" : "all"});

// BIO293_2007HS_Evolution and Ecology of Microorganisms
// https://lms.uzh.ch/url/RepositoryEntry/1376332
identifiers_for_iframe_css_removal.push({"1376332" : "all"});

// Copy of 0 BLAW Online-Pr√ºfung
// https://lms.uzh.ch/url/RepositoryEntry/9440002049
identifiers_for_iframe_css_removal.push({"9440002049" : "all"});

// 14FS 165/557/558m0 Gespr√§chsanalyse (Seminar)
// https://lms.uzh.ch/url/RepositoryEntry/9856516104
identifiers_for_iframe_css_removal.push({"9856516104" : "all"});

// pps ¬´politische prozesse / schweiz¬ª (UniBE)
// https://lms.uzh.ch/url/RepositoryEntry/686555157
identifiers_for_iframe_css_removal.push({"686555157" : "all"});

// Transkribieren nach GAT
// https://lms.uzh.ch/url/RepositoryEntry/1217888258
identifiers_for_iframe_css_removal.push({"1217888258" : "all"});

// BIO126 2008_SS Diversity of Microorganisms
// https://lms.uzh.ch/url/RepositoryEntry/800030736
identifiers_for_iframe_css_removal.push({"800030736" : "all"});

// Gespr√§chsanalyse interaktiv Bochum 2011
// https://lms.uzh.ch/url/RepositoryEntry/3937665024
identifiers_for_iframe_css_removal.push({"3937665024" : "all"});

// gi
// https://lms.uzh.ch/url/RepositoryEntry/1048510488
identifiers_for_iframe_css_removal.push({"1048510488" : "all"});

// Vet: EP 2.4
// https://lms.uzh.ch/url/RepositoryEntry/10792861696
identifiers_for_iframe_css_removal.push({"10792861696" : "all"});

// TEST Alle Bausteine
// https://lms.uzh.ch/url/RepositoryEntry/606961667
identifiers_for_iframe_css_removal.push({"606961667" : "all"});



/*********************************************************************************/
/* (2) Courses with Content Packages based on ELML using custom styles           */
/*********************************************************************************/

// SOREL
// https://lms.uzh.ch/url/RepositoryEntry/1227096085
identifiers_for_iframe_css_removal.push({"1227096085" : "all"});

// SOREL français
// https://lms.uzh.ch/url/RepositoryEntry/8053325825
identifiers_for_iframe_css_removal.push({"8053325825" : "all"});

// ALPECOLe
// https://lms.uzh.ch/url/RepositoryEntry/4030530
identifiers_for_iframe_css_removal.push({"4030530" : "all"});

// Bioethik
// https://lms.uzh.ch/url/RepositoryEntry/779681810
identifiers_for_iframe_css_removal.push({"779681810" : "all"});

// eFeed – Deutsch
// https://lms.uzh.ch/url/RepositoryEntry/64225282
identifiers_for_iframe_css_removal.push({"64225282" : "all"});

// eFeed – Français
// https://lms.uzh.ch/url/RepositoryEntry/313786389
identifiers_for_iframe_css_removal.push({"313786389" : "all"});

// EyeTeach Kurs
// https://lms.uzh.ch/url/RepositoryEntry/3042639877
identifiers_for_iframe_css_removal.push({"3042639877" : "all"});

// Expra Klipsy & PT FS09 online Kurs
// https://lms.uzh.ch/url/RepositoryEntry/1086783499
identifiers_for_iframe_css_removal.push({"1086783499" : "all"});

// PSYCH_ExpraOnline
// https://lms.uzh.ch/url/RepositoryEntry/1819410437
identifiers_for_iframe_css_removal.push({"1819410437" : "all"});

// FOIS
// https://lms.uzh.ch/url/RepositoryEntry/1060667402
identifiers_for_iframe_css_removal.push({"1060667402" : "all"});

// Gi - Gesprächsanalyse interaktiv 2016
// https://lms.uzh.ch/url/RepositoryEntry/14303854592
identifiers_for_iframe_css_removal.push({"14303854592" : "all"});

// GLOPP
// https://lms.uzh.ch/url/RepositoryEntry/384204801
identifiers_for_iframe_css_removal.push({"384204801" : "all"});

// *GLOPP-CP-Testkurs
// https://lms.uzh.ch/url/RepositoryEntry/4819222529
identifiers_for_iframe_css_removal.push({"4819222529" : "all"});

// GLOPP modulweise
// https://lms.uzh.ch/url/RepositoryEntry/1227096073
identifiers_for_iframe_css_removal.push({"1227096073" : "all"});

// Introduction to Matlab
// https://lms.uzh.ch/url/RepositoryEntry/1969717253
identifiers_for_iframe_css_removal.push({"1969717253" : "all"});

// Einführung in Matlab
// https://lms.uzh.ch/url/RepositoryEntry/1262485508
identifiers_for_iframe_css_removal.push({"1262485508" : "all"});

// MESOSworld: Experimentelles Design
// https://lms.uzh.ch/url/RepositoryEntry/1376374
identifiers_for_iframe_css_removal.push({"1376374" : "all"});

// MESOSworld: Einführung in die Statistik für Soziologen, Teil 2
// https://lms.uzh.ch/url/RepositoryEntry/4030579
identifiers_for_iframe_css_removal.push({"4030579" : "all"});

// Online-Selbstlernmodul Cultural Studies
// DIGIREP Online-Selbstlernmodul "Einführung in die Cultural Studies" (Dr. habil. Andreas Hepp)
// https://lms.uzh.ch/auth/RepositoryEntry/604340235
identifiers_for_iframe_css_removal.push({"604340235" : "all"});

// Avoiding Plagiarism (ISW Economy)
// https://lms.uzh.ch/url/RepositoryEntry/15891857410/CourseNode/91852192046531
identifiers_for_iframe_css_removal.push({"15891857410" : "all"});

// Vet: Histo. Präparate
// https://lms.uzh.ch/url/RepositoryEntry/214401027/CourseNode/72310536722495
identifiers_for_iframe_css_removal.push({"214401027" : "all"});

// Master gi - Vollkurs (Kopie)
// https://lms.uzh.ch/url/RepositoryEntry/16159735886
identifiers_for_iframe_css_removal.push({"16159735886" : "all"});

// Master gi - Vollkurs (Kopie)
// https://lms.uzh.ch/url/RepositoryEntry/16159735874
identifiers_for_iframe_css_removal.push({"16159735874" : "all"});