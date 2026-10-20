# Правила R8 для release-сборки (isMinifyEnabled = true).
# Retrofit, OkHttp, Gson, Hilt, Room и Compose приносят свои правила сами.

# Номера строк в стектрейсах; расшифровка — через build/outputs/mapping/release/mapping.txt
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Дженерики и аннотации нужны Gson и Retrofit (List<CategoryDto>, @SerializedName, suspend-методы)
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod

# DTO сети: Gson работает через рефлексию — поля, конструкторы и константы enum не трогаем.
# @SerializedName страхует имена, а это правило — от удаления «неиспользуемых» полей.
-keep class ed.maevski.hwwach.data.remote.model.** { *; }
