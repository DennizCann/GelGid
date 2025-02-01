package com.denizcan.gelgid.data.model

enum class AssetType(val title: String) {
    BANK_ACCOUNT("Banka Hesabı"),
    REAL_ESTATE("Gayrimenkul"),
    VEHICLE("Araç"),
    GOLD("Altın"),
    STOCK("Hisse Senedi"),
    CRYPTOCURRENCY("Kripto Para"),
    OTHER("Diğer")
}

data class AssetHistory(
    val id: String = "",
    val assetId: String = "",
    val amount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)

data class Asset(
    val id: String = "",
    val userId: String = "",
    val name: String = "",           // Örn: "Ziraat Bankası Hesabı", "Ev", "Araba"
    val type: AssetType = AssetType.OTHER,
    val amount: Double = 0.0,        // Değeri
    val description: String = "",    // Açıklama
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val history: List<AssetHistory> = emptyList()
) 