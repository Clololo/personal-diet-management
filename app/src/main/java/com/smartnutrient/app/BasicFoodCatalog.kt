package com.smartnutrient.app

internal fun basicFoodSpecs(): List<CatalogSpec> = listOf(
    basic("鸡蛋","jidan","jd",143.0,12.6,9.5,3.1,0.7,0.0,0.4,142.0,126.0,372.0,listOf("水煮蛋","白煮蛋","整蛋"),listOf("鸡蛋"),"水煮","USDA/中国食物成分表","参考值"),
    basic("鸡蛋清","jidanqing","jdq",52.0,10.9,0.2,0.0,0.7,0.0,0.7,166.0,163.0,0.0,listOf("蛋白","蛋清"),listOf("鸡蛋清"),"水煮","USDA","参考值"),
    basic("鸡蛋黄","jidanhuang","jdh",322.0,15.9,26.5,9.6,3.6,0.0,0.6,48.0,109.0,1085.0,listOf("蛋黄"),listOf("鸡蛋黄"),"水煮","USDA","参考值"),
    basic("煎鸡蛋","jianjidan","jjd",196.0,13.6,14.8,4.3,0.8,0.0,0.4,207.0,135.0,401.0,listOf("煎蛋","荷包蛋"),listOf("鸡蛋","食用油"),"煎","标准配方估算","估算"),
    basic("炒鸡蛋","chaojidan","cjd",180.0,12.4,13.0,3.8,1.2,0.0,1.0,210.0,130.0,380.0,listOf("炒蛋"),listOf("鸡蛋","食用油"),"炒","标准配方估算","估算"),
    basic("全脂牛奶","quanzhiniunai","qz nn",64.0,3.3,3.6,2.1,4.9,0.0,5.0,43.0,150.0,14.0,listOf("纯牛奶","牛奶","全脂纯牛奶"),listOf("牛奶"),"饮用","USDA/中国食物成分表","参考值"),
    basic("低脂牛奶","dizhiniunai","dz nn",43.0,3.4,1.3,0.8,4.9,0.0,5.0,44.0,150.0,9.0,listOf("低脂纯牛奶"),listOf("牛奶"),"饮用","USDA","参考值"),
    basic("脱脂牛奶","tuozhiniunai","tz nn",35.0,3.4,0.1,0.1,4.9,0.0,5.0,44.0,150.0,2.0,listOf("脱脂纯牛奶"),listOf("牛奶"),"饮用","USDA","参考值"),
    basic("无糖豆浆","wutangdoujiang","wtdj",31.0,3.0,1.6,0.2,1.2,0.6,0.5,4.0,118.0,0.0,listOf("原味豆浆","豆浆"),listOf("大豆"),"煮","USDA/标准配方估算","参考值"),
    basic("甜豆浆","tiandoujiang","tdj",49.0,2.8,1.5,0.2,5.5,0.6,4.5,5.0,115.0,0.0,listOf("加糖豆浆"),listOf("大豆","糖"),"煮","标准配方估算","估算"),
    basic("豆浆粉冲调豆浆","doujiangfenzhongdiao","djfzd",39.0,3.1,1.4,0.3,4.2,0.5,3.5,30.0,120.0,0.0,listOf("冲调豆浆","豆浆粉"),listOf("豆浆粉","水"),"冲调","标准配方估算","估算"),
    basic("无糖酸奶","wutangsuanali","wtsn",63.0,5.3,3.3,2.1,4.7,0.0,4.7,46.0,234.0,13.0,listOf("原味酸奶"),listOf("牛奶","乳酸菌"),"发酵","USDA","参考值"),
    basic("甜酸奶","tiansuannai","tsn",89.0,3.0,2.4,1.5,14.0,0.0,13.0,40.0,140.0,10.0,listOf("风味酸奶","加糖酸奶"),listOf("牛奶","糖","乳酸菌"),"发酵","标准配方估算","估算"),
    basic("奶酪","nailao","nl",402.0,24.9,33.1,21.1,1.3,0.0,0.5,621.0,98.0,105.0,listOf("芝士","干酪"),listOf("牛奶"),"发酵","USDA","参考值"),
    basic("原味豆干","yuanweidoudgan","ywdg",140.0,16.2,3.8,0.7,6.9,0.8,0.6,330.0,140.0,0.0,listOf("白豆干","豆干"),listOf("大豆"),"压制","中国食物成分表","参考值"),
    basic("北豆腐","beidoufu","bdf",116.0,12.2,4.8,0.7,3.0,0.5,0.6,15.0,127.0,0.0,listOf("老豆腐","豆腐"),listOf("大豆"),"凝固","中国食物成分表","参考值"),
    basic("南豆腐","nandoufu","ndf",61.0,6.2,2.5,0.4,2.4,0.3,0.5,8.0,125.0,0.0,listOf("嫩豆腐","豆腐"),listOf("大豆"),"凝固","中国食物成分表","参考值")
)

private fun basic(name: String, pinyin: String, initials: String, kcal: Double, protein: Double, fat: Double, sat: Double, carbs: Double, fiber: Double, sugar: Double, sodium: Double, potassium: Double, cholesterol: Double, aliases: List<String>, ingredients: List<String>, method: String, source: String, confidence: String) = CatalogSpec(name, pinyin, initials, "基础食物", "每100g", kcal, protein, fat, sat, carbs, fiber, sugar, sodium, potassium, cholesterol, aliases, ingredients, method, source, confidence)
