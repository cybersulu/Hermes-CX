package com.example.data.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class UnitCategory(
    val id: String,
    val displayName: String
) {
    LENGTH("length", "Length"),
    MASS("mass", "Weight & Mass"),
    TEMPERATURE("temperature", "Temperature"),
    VOLUME("volume", "Volume"),
    AREA("area", "Area"),
    SPEED("speed", "Speed"),
    TIME("time", "Time"),
    DIGITAL("digital", "Digital Storage"),
    PRESSURE("pressure", "Pressure"),
    ENERGY("energy", "Energy & Power")
}

data class ConversionUnit(
    val id: String,
    val name: String,
    val symbol: String,
    val category: UnitCategory,
    val toBaseFactor: Double = 1.0,
    val isBase: Boolean = false
)

object UnitCatalog {

    val lengthUnits: List<ConversionUnit> = listOf(
        ConversionUnit("m", "Meter", "m", UnitCategory.LENGTH, 1.0, isBase = true),
        ConversionUnit("km", "Kilometer", "km", UnitCategory.LENGTH, 1000.0),
        ConversionUnit("cm", "Centimeter", "cm", UnitCategory.LENGTH, 0.01),
        ConversionUnit("mm", "Millimeter", "mm", UnitCategory.LENGTH, 0.001),
        ConversionUnit("in", "Inch", "in", UnitCategory.LENGTH, 0.0254),
        ConversionUnit("ft", "Foot", "ft", UnitCategory.LENGTH, 0.3048),
        ConversionUnit("yd", "Yard", "yd", UnitCategory.LENGTH, 0.9144),
        ConversionUnit("mi", "Mile", "mi", UnitCategory.LENGTH, 1609.344),
        ConversionUnit("nmi", "Nautical Mile", "nmi", UnitCategory.LENGTH, 1852.0),
        ConversionUnit("um", "Micrometer", "µm", UnitCategory.LENGTH, 1e-6),
        ConversionUnit("nm", "Nanometer", "nm", UnitCategory.LENGTH, 1e-9)
    )

    val massUnits: List<ConversionUnit> = listOf(
        ConversionUnit("kg", "Kilogram", "kg", UnitCategory.MASS, 1.0, isBase = true),
        ConversionUnit("g", "Gram", "g", UnitCategory.MASS, 0.001),
        ConversionUnit("mg", "Milligram", "mg", UnitCategory.MASS, 1e-6),
        ConversionUnit("t", "Metric Ton", "t", UnitCategory.MASS, 1000.0),
        ConversionUnit("lb", "Pound", "lb", UnitCategory.MASS, 0.45359237),
        ConversionUnit("oz", "Ounce", "oz", UnitCategory.MASS, 0.028349523125),
        ConversionUnit("st", "Stone", "st", UnitCategory.MASS, 6.35029318),
        ConversionUnit("short_ton", "US Short Ton", "ton", UnitCategory.MASS, 907.18474)
    )

    val temperatureUnits: List<ConversionUnit> = listOf(
        ConversionUnit("c", "Celsius", "°C", UnitCategory.TEMPERATURE, 1.0, isBase = true),
        ConversionUnit("f", "Fahrenheit", "°F", UnitCategory.TEMPERATURE, 1.0),
        ConversionUnit("k", "Kelvin", "K", UnitCategory.TEMPERATURE, 1.0)
    )

    val volumeUnits: List<ConversionUnit> = listOf(
        ConversionUnit("l", "Liter", "L", UnitCategory.VOLUME, 1.0, isBase = true),
        ConversionUnit("ml", "Milliliter", "mL", UnitCategory.VOLUME, 0.001),
        ConversionUnit("m3", "Cubic Meter", "m³", UnitCategory.VOLUME, 1000.0),
        ConversionUnit("cm3", "Cubic Centimeter", "cm³", UnitCategory.VOLUME, 0.001),
        ConversionUnit("gal_us", "US Gallon", "gal", UnitCategory.VOLUME, 3.785411784),
        ConversionUnit("qt_us", "US Quart", "qt", UnitCategory.VOLUME, 0.946352946),
        ConversionUnit("pt_us", "US Pint", "pt", UnitCategory.VOLUME, 0.473176473),
        ConversionUnit("cup_us", "US Cup", "cup", UnitCategory.VOLUME, 0.2365882365),
        ConversionUnit("floz_us", "US Fluid Ounce", "fl oz", UnitCategory.VOLUME, 0.0295735295625),
        ConversionUnit("tbsp", "Tablespoon", "tbsp", UnitCategory.VOLUME, 0.01478676478125),
        ConversionUnit("tsp", "Teaspoon", "tsp", UnitCategory.VOLUME, 0.00492892159375),
        ConversionUnit("gal_uk", "Imperial Gallon", "UK gal", UnitCategory.VOLUME, 4.54609)
    )

    val areaUnits: List<ConversionUnit> = listOf(
        ConversionUnit("m2", "Square Meter", "m²", UnitCategory.AREA, 1.0, isBase = true),
        ConversionUnit("km2", "Square Kilometer", "km²", UnitCategory.AREA, 1e6),
        ConversionUnit("cm2", "Square Centimeter", "cm²", UnitCategory.AREA, 0.0001),
        ConversionUnit("ha", "Hectare", "ha", UnitCategory.AREA, 10000.0),
        ConversionUnit("ac", "Acre", "ac", UnitCategory.AREA, 4046.8564224),
        ConversionUnit("ft2", "Square Foot", "ft²", UnitCategory.AREA, 0.09290304),
        ConversionUnit("in2", "Square Inch", "in²", UnitCategory.AREA, 0.00064516),
        ConversionUnit("yd2", "Square Yard", "yd²", UnitCategory.AREA, 0.83612736),
        ConversionUnit("mi2", "Square Mile", "mi²", UnitCategory.AREA, 2589988.110336)
    )

    val speedUnits: List<ConversionUnit> = listOf(
        ConversionUnit("mps", "Meter per second", "m/s", UnitCategory.SPEED, 1.0, isBase = true),
        ConversionUnit("kmh", "Kilometer per hour", "km/h", UnitCategory.SPEED, 1.0 / 3.6),
        ConversionUnit("mph", "Mile per hour", "mph", UnitCategory.SPEED, 0.44704),
        ConversionUnit("kn", "Knot", "kn", UnitCategory.SPEED, 0.514444444),
        ConversionUnit("fps", "Foot per second", "ft/s", UnitCategory.SPEED, 0.3048)
    )

    val timeUnits: List<ConversionUnit> = listOf(
        ConversionUnit("s", "Second", "s", UnitCategory.TIME, 1.0, isBase = true),
        ConversionUnit("ms", "Millisecond", "ms", UnitCategory.TIME, 0.001),
        ConversionUnit("min", "Minute", "min", UnitCategory.TIME, 60.0),
        ConversionUnit("h", "Hour", "h", UnitCategory.TIME, 3600.0),
        ConversionUnit("d", "Day", "d", UnitCategory.TIME, 86400.0),
        ConversionUnit("wk", "Week", "wk", UnitCategory.TIME, 604800.0),
        ConversionUnit("mo", "Month (30 days)", "mo", UnitCategory.TIME, 2592000.0),
        ConversionUnit("yr", "Year (365 days)", "yr", UnitCategory.TIME, 31536000.0)
    )

    val digitalUnits: List<ConversionUnit> = listOf(
        ConversionUnit("b", "Byte", "B", UnitCategory.DIGITAL, 1.0, isBase = true),
        ConversionUnit("bit", "Bit", "bit", UnitCategory.DIGITAL, 0.125),
        ConversionUnit("kb", "Kilobyte (decimal)", "KB", UnitCategory.DIGITAL, 1000.0),
        ConversionUnit("mb", "Megabyte (decimal)", "MB", UnitCategory.DIGITAL, 1e6),
        ConversionUnit("gb", "Gigabyte (decimal)", "GB", UnitCategory.DIGITAL, 1e9),
        ConversionUnit("tb", "Terabyte (decimal)", "TB", UnitCategory.DIGITAL, 1e12),
        ConversionUnit("pb", "Petabyte (decimal)", "PB", UnitCategory.DIGITAL, 1e15),
        ConversionUnit("kib", "Kibibyte (binary)", "KiB", UnitCategory.DIGITAL, 1024.0),
        ConversionUnit("mib", "Mebibyte (binary)", "MiB", UnitCategory.DIGITAL, 1048576.0),
        ConversionUnit("gib", "Gibibyte (binary)", "GiB", UnitCategory.DIGITAL, 1073741824.0),
        ConversionUnit("tib", "Tebibyte (binary)", "TiB", UnitCategory.DIGITAL, 1099511627776.0)
    )

    val pressureUnits: List<ConversionUnit> = listOf(
        ConversionUnit("pa", "Pascal", "Pa", UnitCategory.PRESSURE, 1.0, isBase = true),
        ConversionUnit("kpa", "Kilopascal", "kPa", UnitCategory.PRESSURE, 1000.0),
        ConversionUnit("bar", "Bar", "bar", UnitCategory.PRESSURE, 100000.0),
        ConversionUnit("mbar", "Millibar", "mbar", UnitCategory.PRESSURE, 100.0),
        ConversionUnit("psi", "Pound per sq. inch", "psi", UnitCategory.PRESSURE, 6894.757293168),
        ConversionUnit("atm", "Atmosphere", "atm", UnitCategory.PRESSURE, 101325.0),
        ConversionUnit("mmhg", "Millimeter of mercury", "mmHg", UnitCategory.PRESSURE, 133.322387415)
    )

    val energyUnits: List<ConversionUnit> = listOf(
        ConversionUnit("j", "Joule", "J", UnitCategory.ENERGY, 1.0, isBase = true),
        ConversionUnit("kj", "Kilojoule", "kJ", UnitCategory.ENERGY, 1000.0),
        ConversionUnit("cal", "Calorie", "cal", UnitCategory.ENERGY, 4.184),
        ConversionUnit("kcal", "Kilocalorie", "kcal", UnitCategory.ENERGY, 4184.0),
        ConversionUnit("wh", "Watt-hour", "Wh", UnitCategory.ENERGY, 3600.0),
        ConversionUnit("kwh", "Kilowatt-hour", "kWh", UnitCategory.ENERGY, 3.6e6),
        ConversionUnit("btu", "British Thermal Unit", "BTU", UnitCategory.ENERGY, 1055.056),
        ConversionUnit("ftlb", "Foot-pound", "ft⋅lb", UnitCategory.ENERGY, 1.3558179483314)
    )

    val allCategories: List<UnitCategory> = UnitCategory.entries

    fun getUnitsForCategory(category: UnitCategory): List<ConversionUnit> {
        return when (category) {
            UnitCategory.LENGTH -> lengthUnits
            UnitCategory.MASS -> massUnits
            UnitCategory.TEMPERATURE -> temperatureUnits
            UnitCategory.VOLUME -> volumeUnits
            UnitCategory.AREA -> areaUnits
            UnitCategory.SPEED -> speedUnits
            UnitCategory.TIME -> timeUnits
            UnitCategory.DIGITAL -> digitalUnits
            UnitCategory.PRESSURE -> pressureUnits
            UnitCategory.ENERGY -> energyUnits
        }
    }

    fun getDefaultUnitsForCategory(category: UnitCategory): Pair<ConversionUnit, ConversionUnit> {
        return when (category) {
            UnitCategory.LENGTH -> Pair(getUnit("m") ?: lengthUnits[0], getUnit("ft") ?: lengthUnits[1])
            UnitCategory.MASS -> Pair(getUnit("kg") ?: massUnits[0], getUnit("lb") ?: massUnits[1])
            UnitCategory.TEMPERATURE -> Pair(getUnit("c") ?: temperatureUnits[0], getUnit("f") ?: temperatureUnits[1])
            UnitCategory.VOLUME -> Pair(getUnit("l") ?: volumeUnits[0], getUnit("gal_us") ?: volumeUnits[1])
            UnitCategory.AREA -> Pair(getUnit("m2") ?: areaUnits[0], getUnit("ft2") ?: areaUnits[1])
            UnitCategory.SPEED -> Pair(getUnit("kmh") ?: speedUnits[0], getUnit("mph") ?: speedUnits[1])
            UnitCategory.TIME -> Pair(getUnit("h") ?: timeUnits[0], getUnit("min") ?: timeUnits[1])
            UnitCategory.DIGITAL -> Pair(getUnit("gb") ?: digitalUnits[0], getUnit("mb") ?: digitalUnits[1])
            UnitCategory.PRESSURE -> Pair(getUnit("bar") ?: pressureUnits[0], getUnit("psi") ?: pressureUnits[1])
            UnitCategory.ENERGY -> Pair(getUnit("kcal") ?: energyUnits[0], getUnit("kj") ?: energyUnits[1])
        }
    }

    private val allUnitsById: Map<String, ConversionUnit> by lazy {
        (lengthUnits + massUnits + temperatureUnits + volumeUnits + areaUnits +
                speedUnits + timeUnits + digitalUnits + pressureUnits + energyUnits)
            .associateBy { it.id }
    }

    fun getUnit(id: String): ConversionUnit? = allUnitsById[id]

    fun convert(value: Double, from: ConversionUnit, to: ConversionUnit): Double {
        if (from.id == to.id) return value
        if (from.category != to.category) return 0.0

        if (from.category == UnitCategory.TEMPERATURE) {
            // Temperature conversion via Celsius base
            val inCelsius = when (from.id) {
                "c" -> value
                "f" -> (value - 32.0) * 5.0 / 9.0
                "k" -> value - 273.15
                else -> value
            }
            return when (to.id) {
                "c" -> inCelsius
                "f" -> (inCelsius * 9.0 / 5.0) + 32.0
                "k" -> inCelsius + 273.15
                else -> inCelsius
            }
        }

        // Linear conversion: value -> base unit -> target unit
        val valueInBase = value * from.toBaseFactor
        return if (to.toBaseFactor != 0.0) valueInBase / to.toBaseFactor else 0.0
    }

    fun getFormula(from: ConversionUnit, to: ConversionUnit): String {
        if (from.id == to.id) return "1 ${from.symbol} = 1 ${to.symbol}"

        if (from.category == UnitCategory.TEMPERATURE) {
            return when {
                from.id == "c" && to.id == "f" -> "°F = (°C × 9/5) + 32"
                from.id == "f" && to.id == "c" -> "°C = (°F - 32) × 5/9"
                from.id == "c" && to.id == "k" -> "K = °C + 273.15"
                from.id == "k" && to.id == "c" -> "°C = K - 273.15"
                from.id == "f" && to.id == "k" -> "K = (°F - 32) × 5/9 + 273.15"
                from.id == "k" && to.id == "f" -> "°F = (K - 273.15) × 9/5 + 32"
                else -> ""
            }
        }

        val oneToTarget = convert(1.0, from, to)
        val df = DecimalFormat("#.######", DecimalFormatSymbols(Locale.US))
        return "1 ${from.symbol} = ${df.format(oneToTarget)} ${to.symbol}"
    }

    fun getQuickPresets(category: UnitCategory): List<Double> {
        return when (category) {
            UnitCategory.TEMPERATURE -> listOf(0.0, 20.0, 37.0, 72.0, 100.0, 212.0)
            UnitCategory.TIME -> listOf(1.0, 5.0, 15.0, 30.0, 60.0, 1440.0)
            UnitCategory.DIGITAL -> listOf(1.0, 8.0, 64.0, 128.0, 512.0, 1024.0)
            else -> listOf(1.0, 5.0, 10.0, 25.0, 50.0, 100.0)
        }
    }
}
