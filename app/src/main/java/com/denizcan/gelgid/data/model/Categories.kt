package com.denizcan.gelgid.data.model

enum class ExpenseCategory(val title: String) {
    FOOD("Yiyecek & İçecek"),
    SHOPPING("Alışveriş"),
    TRANSPORTATION("Ulaşım"),
    HOUSING("Ev & Kira"),
    BILLS("Faturalar"),
    HEALTH("Sağlık"),
    EDUCATION("Eğitim"),
    ENTERTAINMENT("Eğlence"),
    PERSONAL_CARE("Kişisel Bakım"),
    CLOTHING("Giyim"),
    INSURANCE("Sigorta"),
    GIFTS("Hediyeler"),
    ELECTRONICS("Elektronik"),
    SPORTS("Spor"),
    PETS("Evcil Hayvan"),
    TRAVEL("Seyahat"),
    CAR("Araba Giderleri"),
    MAINTENANCE("Bakım & Tamir"),
    SUBSCRIPTIONS("Abonelikler"),
    OTHER("Diğer")
}

enum class IncomeCategory(val title: String) {
    SALARY("Maaş"),
    BONUS("Prim & Bonus"),
    FREELANCE("Serbest Çalışma"),
    RENTAL("Kira Geliri"),
    INVESTMENT("Yatırım Geliri"),
    INTEREST("Faiz Geliri"),
    PENSION("Emekli Maaşı"),
    SCHOLARSHIP("Burs"),
    GIFT("Hediye"),
    SALE("Satış Geliri"),
    DIVIDEND("Temettü"),
    SIDE_BUSINESS("Ek İş"),
    REFUND("İade & Geri Ödeme"),
    OTHER("Diğer")
}

// Seçilen işlem tipine göre kategorileri döndüren yardımcı fonksiyon
fun getCategories(type: TransactionType): List<String> {
    return when (type) {
        TransactionType.EXPENSE -> ExpenseCategory.values().map { it.title }
        TransactionType.INCOME -> IncomeCategory.values().map { it.title }
    }
} 