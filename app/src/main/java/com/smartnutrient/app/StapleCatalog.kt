package com.smartnutrient.app

private data class StapleBase(val name: String, val pinyin: String, val initials: String, val kcal: Double, val protein: Double, val fat: Double, val carbs: Double, val fiber: Double, val sodium: Double, val base: String, val method: String)

internal fun generatedStaples(): List<CatalogSpec> {
    val items = listOf(
        StapleBase("米饭","mifan","mf",116.0,2.6,0.3,25.9,0.3,1.0,"大米","蒸"),
        StapleBase("糙米饭","caomifan","cmf",111.0,2.6,0.9,23.0,1.8,5.0,"糙米","蒸"),
        StapleBase("紫米饭","zimifan","zmf",122.0,2.8,0.7,26.0,1.4,3.0,"紫米","蒸"),
        StapleBase("黑米饭","heimifan","hmf",115.0,3.1,0.8,24.0,2.2,4.0,"黑米","蒸"),
        StapleBase("红米饭","hongmifan","hmf2",118.0,3.0,0.8,24.8,1.9,4.0,"红米","蒸"),
        StapleBase("糯米饭","nuomifan","nmf",166.0,3.8,0.5,36.0,0.8,2.0,"糯米","蒸"),
        StapleBase("小米饭","xiaomifan","xmf",114.0,3.0,0.9,23.0,1.5,2.0,"小米","蒸"),
        StapleBase("藜麦饭","limaifan","lmf",120.0,4.4,1.9,21.3,2.8,7.0,"藜麦","蒸"),
        StapleBase("燕麦饭","yanmaifan","ymf",118.0,4.0,2.0,20.0,3.0,3.0,"燕麦","蒸"),
        StapleBase("大麦饭","damaifan","dmf",123.0,3.0,0.6,26.0,3.8,3.0,"大麦","蒸"),
        StapleBase("薏米饭","yimifan","ymf2",121.0,3.2,0.9,24.5,2.0,3.0,"薏米","蒸"),
        StapleBase("高粱米饭","gaoliangmifan","glmf",125.0,3.0,0.7,26.0,2.1,3.0,"高粱米","蒸"),
        StapleBase("荞麦饭","qiaomaifan","qmf",114.0,3.4,0.8,22.5,2.4,2.0,"荞麦","蒸"),
        StapleBase("玉米饭","yumifan","ymf3",120.0,3.4,1.3,25.0,2.2,2.0,"玉米糁","蒸"),
        StapleBase("白粥","baizhou","bz",46.0,1.1,0.3,9.8,0.3,1.0,"大米","煮"),
        StapleBase("小米粥","xiaomizhou","xmz",46.0,1.4,0.7,8.4,0.7,1.0,"小米","煮"),
        StapleBase("燕麦粥","yanmaizhou","ymz",68.0,2.4,1.4,12.0,1.7,2.0,"燕麦","煮"),
        StapleBase("玉米粥","yumizhou","ymz2",45.0,1.1,0.5,9.8,0.8,2.0,"玉米糁","煮"),
        StapleBase("八宝粥","babaozhou","bbz",85.0,3.0,1.0,17.0,1.5,30.0,"杂粮","煮"),
        StapleBase("紫米粥","zimizhou","zmz",48.0,1.5,0.4,10.0,0.7,1.0,"紫米","煮"),
        StapleBase("南瓜粥","nanguazhou","ngz",38.0,1.0,0.2,8.0,0.6,3.0,"大米,南瓜","煮"),
        StapleBase("红薯粥","hongshuzhou","hsz",49.0,1.2,0.2,10.5,0.8,5.0,"大米,红薯","煮"),
        StapleBase("皮蛋瘦肉粥","pidanshourouzhou","pd srz",59.0,3.0,1.5,8.5,0.3,320.0,"大米,皮蛋,瘦肉","煮"),
        StapleBase("鸡肉粥","jirouzhou","jrz",55.0,3.2,1.1,8.4,0.2,240.0,"大米,鸡肉","煮"),
        StapleBase("海鲜粥","haixianzhou","hxz",60.0,3.8,1.2,8.8,0.3,360.0,"大米,海鲜","煮"),
        StapleBase("馒头","mantou","mt",221.0,7.0,1.1,47.0,1.3,165.0,"小麦粉","蒸"),
        StapleBase("全麦馒头","quanmaimantou","qmmt",205.0,8.5,1.2,41.0,5.2,180.0,"全麦粉","蒸"),
        StapleBase("玉米窝头","yumiwotou","ymwt",195.0,5.5,1.5,41.0,3.0,180.0,"玉米面","蒸"),
        StapleBase("荞麦馒头","qiaomaimantou","qmmmt",202.0,7.4,1.2,42.0,4.2,175.0,"荞麦粉","蒸"),
        StapleBase("花卷","huajuan","hj",217.0,6.4,2.1,44.0,1.5,210.0,"小麦粉","蒸"),
        StapleBase("红糖馒头","hongtangmantou","htmt",235.0,6.5,1.2,50.0,1.2,160.0,"小麦粉,红糖","蒸"),
        StapleBase("菜包","caibao","cb",160.0,4.5,3.0,28.0,2.4,320.0,"小麦粉,蔬菜","蒸"),
        StapleBase("肉包","roubao","rb",227.0,9.2,7.0,30.0,1.5,450.0,"小麦粉,猪肉","蒸"),
        StapleBase("豆沙包","doushabao","dsb",240.0,6.0,3.0,45.0,2.0,220.0,"小麦粉,红豆","蒸"),
        StapleBase("叉烧包","chashaobao","csb",242.0,8.0,6.0,39.0,1.0,420.0,"小麦粉,叉烧","蒸"),
        StapleBase("流沙包","liushabao","lsb",260.0,6.8,8.0,40.0,0.8,250.0,"小麦粉,蛋黄","蒸"),
        StapleBase("奶黄包","naihuangbao","nhb",250.0,6.5,7.0,41.0,0.7,230.0,"小麦粉,奶黄","蒸"),
        StapleBase("烧麦","shaomai","sm",220.0,7.0,8.0,30.0,1.0,420.0,"面皮,糯米","蒸"),
        StapleBase("蒸饺","zhengjiao","zj",180.0,7.0,5.0,26.0,1.3,360.0,"面皮,馅料","蒸"),
        StapleBase("水饺","shuijiao","sj",200.0,8.0,6.0,28.0,1.5,420.0,"面皮,馅料","煮"),
        StapleBase("馄饨","huntun","ht",170.0,7.0,5.0,24.0,1.0,390.0,"面皮,馅料","煮"),
        StapleBase("煎饺","jianjiao","jj",230.0,7.0,10.0,28.0,1.3,430.0,"面皮,馅料","煎"),
        StapleBase("白面条","baimiantiao","bmt",137.0,4.5,0.7,28.0,1.2,4.0,"小麦粉","煮"),
        StapleBase("拉面","lamian","lm",147.0,5.0,1.1,29.0,1.4,430.0,"小麦粉,碱水","拉,煮"),
        StapleBase("刀削面","daoxiaomian","dxm",151.0,5.3,1.0,30.0,1.4,170.0,"小麦粉","削,煮"),
        StapleBase("手擀面","shouganmian","sgm",145.0,5.2,1.0,29.0,1.3,8.0,"小麦粉","擀,煮"),
        StapleBase("全麦面条","quanmaimiantiao","qmmt2",140.0,5.7,1.0,27.0,3.5,6.0,"全麦粉","煮"),
        StapleBase("荞麦面","qiaomaimian","qmm",129.0,5.0,0.6,26.0,2.0,6.0,"荞麦粉","煮"),
        StapleBase("乌冬面","wudongmian","wdm",137.0,4.0,0.5,29.0,1.1,180.0,"小麦粉","煮"),
        StapleBase("米线","mixian","mx",100.0,1.8,0.2,23.0,0.4,2.0,"大米","煮"),
        StapleBase("河粉","hefen","hf",110.0,1.5,0.4,25.0,0.3,12.0,"大米","蒸,煮"),
        StapleBase("米粉","mifen","mf2",105.0,1.6,0.3,24.0,0.4,5.0,"大米","煮"),
        StapleBase("粉丝","fensi","fs",82.0,0.2,0.1,20.0,0.3,4.0,"淀粉","煮"),
        StapleBase("红薯粉条","hongshufentiao","hsft",110.0,0.2,0.1,27.0,0.5,5.0,"红薯淀粉","煮"),
        StapleBase("意大利面","yidalimian","ydlm",158.0,5.8,0.9,31.0,1.8,5.0,"小麦粉","煮"),
        StapleBase("全麦意面","quanmaiyimian","qmym",149.0,5.3,0.9,30.0,3.2,4.0,"全麦粉","煮"),
        StapleBase("通心粉","tongxinfen","txf",160.0,5.9,1.0,31.0,1.8,6.0,"小麦粉","煮"),
        StapleBase("凉面","liangmian","lm2",155.0,5.0,3.0,27.0,1.4,380.0,"小麦粉","凉拌"),
        StapleBase("热干面（熟）","reganmian","rgm",184.0,5.3,7.0,25.0,1.5,530.0,"碱面,芝麻酱","拌"),
        StapleBase("炸酱面","zhajiangmian","zjm",180.0,7.0,6.0,25.0,1.5,560.0,"面条,炸酱","拌"),
        StapleBase("担担面","dandanmian","ddm",190.0,7.0,8.0,23.0,1.5,620.0,"面条,辣酱","拌"),
        StapleBase("重庆小面","chongqingxiaomian","cqxm",175.0,5.0,7.0,23.0,1.5,650.0,"面条,辣油","煮,拌"),
        StapleBase("牛肉面","niuroumian","nrm",155.0,7.0,4.0,23.0,1.3,470.0,"面条,牛肉","煮"),
        StapleBase("兰州牛肉面","lanzhouniuroumian","lznrm",158.0,7.2,4.3,23.5,1.3,510.0,"拉面,牛肉","拉,煮"),
        StapleBase("番茄鸡蛋面","fanqiejidanmian","fqjdm",145.0,6.0,4.5,21.0,1.0,400.0,"面条,番茄,鸡蛋","煮"),
        StapleBase("酸辣粉","suanlafen","slf",135.0,2.0,5.0,20.0,1.0,620.0,"红薯粉","煮"),
        StapleBase("螺蛳粉","luosifen","lsf",165.0,5.0,6.0,23.0,1.4,760.0,"米粉,螺蛳汤","煮"),
        StapleBase("桂林米粉","guilinmifen","glmf",160.0,5.0,5.0,24.0,1.0,580.0,"米粉","煮"),
        StapleBase("云南米线","yunnanmixian","ynmx",145.0,4.0,4.0,23.0,0.9,510.0,"米线","煮"),
        StapleBase("方便面","fangbianmian","fbm",250.0,6.0,10.0,34.0,1.5,900.0,"油炸面饼","泡"),
        StapleBase("非油炸方便面","feiyouzhafangbianmian","fyzfbm",350.0,9.0,4.0,67.0,2.5,800.0,"面饼","泡"),
        StapleBase("年糕","niangao","ng",154.0,3.0,0.5,34.0,0.6,30.0,"大米","蒸,炒"),
        StapleBase("炒年糕","chaoniangao","cng",185.0,3.5,5.0,32.0,0.8,430.0,"年糕","炒"),
        StapleBase("汤年糕","tangniangao","tng",125.0,3.0,2.0,23.0,0.6,380.0,"年糕","煮"),
        StapleBase("玉米棒","yumibang","ymb",112.0,4.0,1.2,23.0,2.9,1.0,"玉米","煮"),
        StapleBase("烤玉米","kaoyumi","kym",125.0,4.3,1.8,23.0,2.8,90.0,"玉米","烤"),
        StapleBase("红薯","hongshu","hs",86.0,1.6,0.1,20.0,3.0,55.0,"红薯","蒸"),
        StapleBase("烤红薯","kaohongshu","khs",92.0,1.7,0.1,22.0,3.0,60.0,"红薯","烤"),
        StapleBase("土豆","tudou","td",77.0,2.0,0.1,17.0,2.2,5.0,"土豆","蒸"),
        StapleBase("山药","shanyao","sy",57.0,1.9,0.2,12.0,0.8,5.0,"山药","蒸"),
        StapleBase("芋头","yutou","yt",79.0,2.2,0.2,18.0,2.1,30.0,"芋头","蒸"),
        StapleBase("南瓜","nangua","ng2",26.0,1.0,0.1,6.5,0.5,1.0,"南瓜","蒸"),
        StapleBase("藕粉","oufen","of",372.0,0.2,0.1,92.0,0.4,20.0,"莲藕淀粉","冲泡"),
        StapleBase("荞麦凉粉","qiaomailiangfen","qmlf",80.0,0.6,0.2,19.0,0.8,210.0,"荞麦淀粉","凉拌"),
        StapleBase("豌豆凉粉","wandouliangfen","wdlf",85.0,1.0,0.2,20.0,0.8,220.0,"豌豆淀粉","凉拌"),
        StapleBase("肠粉","changfen","cf",125.0,3.0,4.0,20.0,0.5,350.0,"米浆","蒸"),
        StapleBase("皮蛋瘦肉粥","pidanshourouzhou2","pdsrz2",59.0,3.0,1.5,8.5,0.3,320.0,"大米,皮蛋,瘦肉","煮"),
        StapleBase("玉米面粥","yumimianzhou","ymmz",47.0,1.2,0.6,9.5,0.9,2.0,"玉米面","煮"),
        StapleBase("黑米粥","heimizhou","hmz",52.0,1.6,0.5,10.5,0.9,2.0,"黑米","煮"),
        StapleBase("红豆粥","hongdouzhou","hdz",60.0,2.2,0.5,12.0,1.6,2.0,"红豆,大米","煮"),
        StapleBase("绿豆粥","lvdouzhou","ldz",58.0,2.0,0.4,11.8,1.5,2.0,"绿豆,大米","煮")
    )
    val out = mutableListOf<CatalogSpec>()
    items.forEach { item ->
        out += CatalogSpec(item.name, item.pinyin, item.initials, "主食", "熟重", item.kcal, item.protein, item.fat, item.fat * 0.25, item.carbs, item.fiber, 0.0, item.sodium, 100.0, 0.0, emptyList(), listOf(item.base), item.method, "中国食物成分表/USDA 基础食材 + 标准配方估算", "估算")
        if (item.name in RAW_WEIGHT_STAPLES) {
            out += CatalogSpec("${item.name}（干重）", item.pinyin, item.initials, "主食", "干重", item.kcal * 2.2, item.protein * 2.2, item.fat * 2.2, item.fat * 0.55, item.carbs * 2.2, item.fiber * 2.2, 0.0, item.sodium * 2.0, item.kcal * 1.0, 0.0, emptyList(), listOf(item.base), "干重原料", "生熟换算估算", "估算")
        }
    }
    return out
}

private val RAW_WEIGHT_STAPLES = setOf(
    "米饭","糙米饭","紫米饭","黑米饭","红米饭","小米饭","藜麦饭","燕麦饭","大麦饭","薏米饭",
    "高粱米饭","荞麦饭","玉米饭","白粥","小米粥","燕麦粥","玉米粥","八宝粥","紫米粥","南瓜粥",
    "红薯粥","皮蛋瘦肉粥","鸡肉粥","海鲜粥",
    "白面条","拉面","刀削面","手擀面","全麦面条","荞麦面","乌冬面","米线",
    "河粉","米粉","粉丝","意大利面","全麦意面","通心粉","年糕","红薯粉条","肠粉"
)


