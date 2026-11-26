package com.smartnutrient.app

data class CatalogSpec(
    val name: String,
    val pinyin: String,
    val initials: String,
    val category: String,
    val basis: String,
    val kcal: Double,
    val protein: Double,
    val fat: Double,
    val sat: Double,
    val carbs: Double,
    val fiber: Double,
    val sugar: Double,
    val sodium: Double,
    val potassium: Double,
    val cholesterol: Double,
    val aliases: List<String> = emptyList(),
    val ingredients: List<String> = emptyList(),
    val method: String = "",
    val source: String = "标准配方估算",
    val confidence: String = "估算"
)

private data class StapleSpec(
    val name: String,
    val pinyin: String,
    val initials: String,
    val kcal: Double,
    val protein: Double,
    val fat: Double,
    val sat: Double,
    val carbs: Double,
    val fiber: Double,
    val sugar: Double,
    val sodium: Double,
    val potassium: Double,
    val base: String,
    val method: String
)

object FoodCatalog {
    private val core: List<CatalogSpec> = listOf(
        core("米饭", "mifan", "mf", "主食", "熟重", 116.0, 2.6, 0.3, 0.1, 25.9, 0.3, 0.0, 1.0, 30.0, 0.0, listOf("熟米饭"), listOf("大米"), "蒸煮", "中国食物成分表", "参考值"),
            core("生大米", "shengdam i", "sdm", "主食", "生重", 346.0, 7.6, 0.8, 0.2, 77.2, 0.6, 0.1, 3.0, 103.0, 0.0, listOf("生米"), listOf("大米"), "原料", "中国食物成分表", "参考值"),
        core("全麦意面（熟）", "quanmaiyimian", "qmym", "主食", "熟重", 149.0, 5.3, 0.9, 0.2, 30.0, 3.2, 0.6, 4.0, 44.0, 0.0, listOf("全麦意面"), listOf("小麦"), "煮", "USDA", "参考值"),
        core("热干面", "reganmian", "rgm", "主食", "每份", 368.0, 10.6, 14.0, 3.0, 50.0, 3.0, 2.0, 1060.0, 200.0, 0.0, listOf("武汉热干面", "每份200g"), listOf("面条", "芝麻酱"), "拌", "标准配方估算", "估算"),
        core("油炸方便面饼", "youzhafangbianmian", "yzfbm", "主食", "干重", 473.0, 9.5, 21.1, 10.0, 61.0, 2.3, 2.0, 1140.0, 134.0, 0.0, listOf("方便面", "泡面"), listOf("小麦"), "油炸", "包装营养标签典型值", "估算"),
        core("番茄炒蛋", "fanqiechaodan", "fqcd", "菜", "熟重", 105.0, 5.8, 7.4, 1.8, 4.6, 0.7, 2.7, 270.0, 190.0, 186.0, listOf("西红柿炒鸡蛋", "xihongshi"), listOf("番茄", "鸡蛋"), "炒", "标准配方估算", "估算"),
        core("清蒸鲈鱼", "qingzhengluyu", "qzly", "菜", "熟重", 105.0, 18.6, 3.0, 0.7, 0.0, 0.0, 0.0, 350.0, 310.0, 86.0, listOf("蒸鱼", "鲈鱼"), listOf("鲈鱼"), "蒸", "标准配方估算", "估算"),
        core("青椒肉丝", "qingjiaorousi", "qjrs", "菜", "熟重", 142.0, 10.3, 9.8, 3.2, 3.2, 1.0, 1.4, 430.0, 230.0, 40.0, listOf("辣椒炒肉"), listOf("青椒", "猪肉"), "炒", "标准配方估算", "估算"),
        core("西兰花（焯水）", "xilanhua", "xlh", "菜", "熟重", 35.0, 2.4, 0.4, 0.1, 7.2, 3.3, 1.4, 18.0, 293.0, 0.0, listOf("西蓝花"), listOf("西兰花"), "焯水", "USDA", "参考值"),
        core("苹果", "pingguo", "pg", "水果", "可食部", 52.0, 0.3, 0.2, 0.0, 13.8, 2.4, 10.4, 1.0, 107.0, 0.0, listOf("红富士"), listOf("苹果"), "鲜食", "USDA/中国食物成分表", "参考值"),
        core("香蕉", "xiangjiao", "xj", "水果", "可食部", 89.0, 1.1, 0.3, 0.1, 22.8, 2.6, 12.2, 1.0, 358.0, 0.0, listOf("芭蕉"), listOf("香蕉"), "鲜食", "USDA/中国食物成分表", "参考值"),
        core("蓝莓", "lanmei", "lm", "水果", "可食部", 57.0, 0.7, 0.3, 0.0, 14.5, 2.4, 10.0, 1.0, 77.0, 0.0, listOf("蓝莓鲜果"), listOf("蓝莓"), "鲜食", "USDA", "参考值")
    )

    private val unusedStaples = listOf(
        StapleSpec("白粥", "baizhou", "bz", 46.0, 1.1, 0.3, 0.1, 9.8, 0.3, 0.1, 1.0, 30.0, "大米", "煮"),
        StapleSpec("小米粥", "xiaomizhou", "xmz", 46.0, 1.4, 0.7, 0.1, 8.4, 0.7, 0.1, 1.0, 48.0, "小米", "煮"),
        StapleSpec("燕麦粥", "yanmaizhou", "ymz", 68.0, 2.4, 1.4, 0.2, 12.0, 1.7, 0.3, 2.0, 70.0, "燕麦", "煮"),
        StapleSpec("八宝粥", "babaozhou", "bbz", 85.0, 3.0, 1.0, 0.2, 17.0, 1.5, 5.0, 30.0, 130.0, "杂粮", "煮"),
        StapleSpec("玉米粥", "yumizhou", "ymz2", 45.0, 1.1, 0.5, 0.1, 9.8, 0.8, 0.4, 2.0, 50.0, "玉米", "煮"),
        StapleSpec("紫米粥", "zimizhou", "zmz", 48.0, 1.5, 0.4, 0.1, 10.0, 0.7, 0.1, 1.0, 45.0, "紫米", "煮"),
        StapleSpec("蒸馒头", "zhengmantou", "zmt", 221.0, 7.0, 1.1, 0.2, 47.0, 1.3, 0.0, 165.0, 125.0, "小麦粉", "蒸"),
        StapleSpec("全麦馒头", "quanmaimantou", "qmmt", 205.0, 8.5, 1.2, 0.2, 41.0, 5.2, 0.0, 180.0, 180.0, "全麦粉", "蒸"),
        StapleSpec("花卷", "huajuan", "hj", 217.0, 6.4, 2.1, 0.3, 44.0, 1.5, 1.0, 210.0, 120.0, "小麦粉", "蒸"),
        StapleSpec("肉包", "roubao", "rb", 227.0, 9.2, 7.0, 2.2, 30.0, 1.5, 2.0, 450.0, 160.0, "小麦粉,猪肉", "蒸"),
        StapleSpec("菜包", "caibao", "cb", 160.0, 4.5, 3.0, 0.5, 28.0, 2.4, 1.5, 320.0, 180.0, "小麦粉,蔬菜", "蒸"),
        StapleSpec("豆沙包", "doushabao", "dsb", 240.0, 6.0, 3.0, 0.5, 45.0, 2.0, 18.0, 220.0, 130.0, "小麦粉,红豆", "蒸"),
        StapleSpec("白面条（熟）", "baimiantiao", "bmt", 137.0, 4.5, 0.7, 0.2, 28.0, 1.2, 0.3, 4.0, 30.0, "小麦粉", "煮"),
        StapleSpec("拉面（熟）", "lamian", "lm", 147.0, 5.0, 1.1, 0.3, 29.0, 1.4, 0.5, 430.0, 50.0, "小麦粉,碱水", "拉,煮"),
        StapleSpec("刀削面（熟）", "daoxiaomian", "dxm", 151.0, 5.3, 1.0, 0.2, 30.0, 1.4, 0.4, 170.0, 60.0, "小麦粉", "削,煮"),
        StapleSpec("荞麦面（熟）", "qiaomaimian", "qmm", 129.0, 5.0, 0.6, 0.1, 26.0, 2.0, 0.4, 6.0, 80.0, "荞麦粉", "煮"),
        StapleSpec("乌冬面（熟）", "wudongmian", "wdm", 137.0, 4.0, 0.5, 0.1, 29.0, 1.1, 0.5, 180.0, 40.0, "小麦粉", "煮"),
        StapleSpec("米线（熟）", "mixian", "mx", 100.0, 1.8, 0.2, 0.0, 23.0, 0.4, 0.1, 2.0, 10.0, "大米", "煮"),
        StapleSpec("河粉（熟）", "hefen", "hf", 110.0, 1.5, 0.4, 0.1, 25.0, 0.3, 0.1, 12.0, 10.0, "大米", "蒸,煮"),
        StapleSpec("粉丝（熟）", "fensi", "fs", 82.0, 0.2, 0.1, 0.0, 20.0, 0.3, 0.0, 4.0, 8.0, "淀粉", "煮"),
        StapleSpec("年糕", "niangao", "ng", 154.0, 3.0, 0.5, 0.1, 34.0, 0.6, 0.4, 30.0, 20.0, "大米", "蒸,炒"),
        StapleSpec("糯米饭", "nuomifan", "nmf", 166.0, 3.8, 0.5, 0.1, 36.0, 0.8, 0.2, 2.0, 40.0, "糯米", "蒸"),
        StapleSpec("糙米饭", "caomifan", "cmf", 111.0, 2.6, 0.9, 0.2, 23.0, 1.8, 0.3, 5.0, 80.0, "糙米", "蒸"),
        StapleSpec("紫米饭", "zimifan", "zmf", 122.0, 2.8, 0.7, 0.2, 26.0, 1.4, 0.2, 3.0, 75.0, "紫米", "蒸"),
        StapleSpec("藜麦饭", "limaifan", "lmf", 120.0, 4.4, 1.9, 0.2, 21.3, 2.8, 0.9, 7.0, 170.0, "藜麦", "蒸"),
        StapleSpec("玉米棒（熟）", "yumibang", "ymb", 112.0, 4.0, 1.2, 0.2, 23.0, 2.9, 6.2, 1.0, 270.0, "玉米", "煮"),
        StapleSpec("红薯（蒸）", "hongshu", "hs", 86.0, 1.6, 0.1, 0.0, 20.0, 3.0, 4.2, 55.0, 337.0, "红薯", "蒸"),
        StapleSpec("土豆（蒸）", "tudou", "td", 77.0, 2.0, 0.1, 0.0, 17.0, 2.2, 0.8, 5.0, 425.0, "马铃薯", "蒸"),
        StapleSpec("山药（蒸）", "shanyao", "sy", 57.0, 1.9, 0.2, 0.0, 12.0, 0.8, 0.5, 5.0, 213.0, "山药", "蒸"),
        StapleSpec("芋头（蒸）", "yutou", "yt", 79.0, 2.2, 0.2, 0.0, 18.0, 2.1, 0.5, 30.0, 378.0, "芋头", "蒸"),
        StapleSpec("南瓜（蒸）", "nangua", "ng2", 26.0, 1.0, 0.1, 0.0, 6.5, 0.5, 2.8, 1.0, 340.0, "南瓜", "蒸"),
        StapleSpec("藕粉", "oufen", "of", 372.0, 0.2, 0.1, 0.0, 92.0, 0.4, 1.0, 20.0, 10.0, "莲藕淀粉", "冲泡")
    )

    val items: List<FoodItem> = buildList {
        core.forEachIndexed { index, spec -> add(spec.toFood(index + 1)) }
        generatedStaples().forEachIndexed { index, spec -> add(spec.toFood(500 + index)) }
        listOf(
            core("生白米","shengbaifan","sbf","主食","生重",346.0,7.6,0.8,0.2,77.2,0.6,0.1,3.0,103.0,0.0,listOf("大米","生米"),listOf("大米"),"原料","中国食物成分表","参考值"),
            core("生糙米","shengcaomi","scm","主食","生重",348.0,7.7,2.7,0.6,74.0,3.5,0.5,7.0,250.0,0.0,listOf("糙米"),listOf("糙米"),"原料","USDA","参考值"),
            core("生小米","shengxiaomi","sxm","主食","生重",361.0,9.0,3.1,0.5,75.0,1.6,0.1,8.0,284.0,0.0,listOf("小米"),listOf("小米"),"原料","中国食物成分表","参考值"),
            core("生黑米","shengheimi","shm","主食","生重",341.0,9.4,2.5,0.4,72.2,3.9,0.0,7.0,256.0,0.0,listOf("黑米"),listOf("黑米"),"原料","USDA","参考值"),
            core("生糯米","shengnuomi","snm","主食","生重",350.0,7.3,1.0,0.2,78.0,0.8,0.1,5.0,112.0,0.0,listOf("糯米"),listOf("糯米"),"原料","中国食物成分表","参考值"),
            core("生燕麦片","shengyanmaipian","sympt","主食","生重",377.0,13.2,6.5,1.2,67.7,10.1,1.0,6.0,429.0,0.0,listOf("燕麦","燕麦片"),listOf("燕麦"),"原料","USDA","参考值"),
            core("生红米","shenghongmi","shm2","主食","生重",360.0,7.9,2.7,0.5,76.0,3.5,0.5,5.0,230.0,0.0,listOf("红米"),listOf("红米"),"原料","USDA 同类谷物估算","估算"),
            core("生荞麦米","shengqiaomai","sqm","主食","生重",343.0,13.2,3.4,0.7,71.5,10.0,0.0,1.0,460.0,0.0,listOf("荞麦"),listOf("荞麦"),"原料","USDA","参考值"),
            core("生薏米","shengyimi","sym","主食","生重",361.0,12.8,3.3,0.7,71.0,2.0,0.0,5.0,250.0,0.0,listOf("薏米"),listOf("薏米"),"原料","中国食物成分表","参考值"),
            core("生藜麦","shenglimai","slm","主食","生重",368.0,14.1,6.1,0.7,64.2,7.0,0.0,5.0,563.0,0.0,listOf("藜麦"),listOf("藜麦"),"原料","USDA","参考值"),
            core("生大麦粒","shengdamaili","sdm l","主食","生重",354.0,12.5,2.3,0.5,73.5,17.3,0.8,9.0,452.0,0.0,listOf("大麦"),listOf("大麦"),"原料","USDA","参考值"),
            core("生玉米糁","shengyumisen","sy ms","主食","生重",365.0,8.1,3.6,0.6,76.9,3.6,0.6,35.0,287.0,0.0,listOf("玉米糁"),listOf("玉米"),"原料","中国食物成分表","参考值"),
            core("生高粱米","shenggaoliang","sglm","主食","生重",351.0,10.4,3.1,0.5,73.0,6.3,0.0,5.0,363.0,0.0,listOf("高粱米"),listOf("高粱"),"原料","USDA","参考值"),
            core("生土豆","shengtudou","std","主食","生重",77.0,2.0,0.1,0.0,17.5,2.2,0.8,6.0,425.0,0.0,listOf("马铃薯","土豆"),listOf("土豆"),"原料","USDA","参考值"),
            core("生红薯","shenghongshu","shs","主食","生重",86.0,1.6,0.1,0.0,20.1,3.0,4.2,55.0,337.0,0.0,listOf("甘薯","地瓜"),listOf("红薯"),"原料","USDA","参考值"),
            core("生山药","shengshanyao","ssy","主食","生重",57.0,1.9,0.2,0.0,12.4,0.8,0.5,5.0,213.0,0.0,listOf("淮山"),listOf("山药"),"原料","中国食物成分表","参考值"),
            core("生芋头","shengyutou","syt","主食","生重",79.0,2.2,0.2,0.0,18.1,2.1,0.5,30.0,378.0,0.0,listOf("芋艿"),listOf("芋头"),"原料","USDA","参考值"),
            core("生南瓜","shengnangua","sng","主食","生重",26.0,1.0,0.1,0.0,6.5,0.5,2.8,1.0,340.0,0.0,listOf("南瓜"),listOf("南瓜"),"原料","USDA","参考值"),
            core("生紫薯","shengzishu","szs","主食","生重",82.0,1.6,0.1,0.0,19.0,3.0,5.0,30.0,350.0,0.0,listOf("紫薯"),listOf("紫薯"),"原料","USDA 同类鲜薯估算","估算"),
            core("生莲藕","shenglianou","slo","主食","生重",74.0,2.6,0.1,0.0,17.2,4.9,0.0,40.0,556.0,0.0,listOf("莲藕","藕"),listOf("莲藕"),"原料","USDA","参考值"),
            core("生荸荠","shengbqi","sbq","主食","生重",97.0,1.4,0.1,0.0,23.9,3.0,4.8,14.0,584.0,0.0,listOf("荸荠","马蹄"),listOf("荸荠"),"原料","USDA","参考值"),
            core("生菱角","shenglingjiao","slj","主食","生重",101.0,4.0,0.1,0.0,21.0,1.7,0.0,15.0,437.0,0.0,listOf("菱角"),listOf("菱角"),"原料","中国食物成分表","参考值"),
            core("生芋圆","shengyuyuan","syy","主食","生重",135.0,0.2,0.1,0.0,33.0,0.2,13.0,5.0,20.0,0.0,listOf("芋圆原料"),listOf("芋头,木薯淀粉"),"原料","标准配方估算","估算")
        ).forEachIndexed { index, spec -> add(spec.toFood(19_000 + index)) }
        buildDishes().forEachIndexed { index, spec -> add(spec.toFood(2000 + index)) }
        fruitSpecs().forEachIndexed { index, spec -> add(spec.toFood(8000 + index)) }
        basicFoodSpecs().forEachIndexed { index, spec -> add(spec.toFood(30_000 + index)) }
        ingredientSpecs().forEachIndexed { index, spec -> add(spec.toFood(40_000 + index)) }
        DrinkCatalog.items.forEach { food ->
            if (none { it.name == food.name && it.basis == "每100g" }) add(food)
        }
        AdditionalFoodCatalog.items.forEach { food ->
            if (none { it.name == food.name && it.basis == food.basis }) add(food)
        }
    }.let { all -> all.filterIndexed { index, food -> all.indexOfFirst { it.name == food.name && it.basis == food.basis } == index } }

    private fun CatalogSpec.toFood(id: Int): FoodItem = FoodItem(
        id, name, pinyin, initials, category, basis,
        FoodNutrients(
            kcal, protein, fat, sat, carbs, fiber, sugar, sodium, potassium, cholesterol,
            SugarDataPolicy.addedSugarG(category, name, sugar),
        ),
        aliases, ingredients, method, source, confidence
    )

    private fun StapleSpec.toFood(id: Int): FoodItem = CatalogSpec(
        name, pinyin, initials, "主食", "熟重",
        kcal, protein, fat, sat, carbs, fiber, sugar, sodium, potassium, 0.0,
        emptyList(), listOf(base), method,
        "中国食物成分表/USDA 基础食材 + 标准配方估算", "估算"
    ).toFood(id)

    private fun core(name: String, pinyin: String, initials: String, category: String, basis: String, kcal: Double, protein: Double, fat: Double, sat: Double, carbs: Double, fiber: Double, sugar: Double, sodium: Double, potassium: Double, cholesterol: Double, aliases: List<String>, ingredients: List<String>, method: String, source: String, confidence: String) =
        CatalogSpec(name, pinyin, initials, category, basis, kcal, protein, fat, sat, carbs, fiber, sugar, sodium, potassium, cholesterol, aliases, ingredients, method, source, confidence)
}



