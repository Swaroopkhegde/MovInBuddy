package com.example.movinbuddy.data

object SectionTemplates {

    // Order also defines the default seed order.
    val ITEM_TEMPLATES: LinkedHashMap<String, List<String>> = linkedMapOf(
        "Exterior" to listOf(
            "Mailbox", "Fences & Gates", "Pool/Spa & Equipment", "Lawn, Trees & Shrubs",
            "Underground Lawn Sprinkler", "Exterior Faucets", "Roof & Gutters", "Siding & Paint",
            "Driveway", "Front Door", "Front Door Lock & Knob", "Front Door Light",
            "Doorbell", "Back Door", "Back Door Lock & Knob", "Back Door Light",
            "Patio or Deck", "Patio Door", "Patio Door Lock & Knob", "Patio Door Light",
            "Water Shut-Off Valve", "Electrical Breaker Panel", "Other"
        ),
        "Garage" to listOf(
            "Ceilings & Walls", "Floor", "Automatic Door Opener", "Safety Reversal Sensor",
            "Remotes", "Garage Doors", "Exterior Doors & Stops", "Storage Room", "Other"
        ),
        "Entry" to listOf(
            "Ceiling & Walls", "Paint & Wallpaper", "Doors & Door Stops", "Door Locks & Knobs",
            "Flooring", "Light Fixtures", "Windows & Screens", "Window Latches",
            "Plugs & Switches", "Closet Shelves & Rods", "Other"
        ),
        "Living Room" to listOf(
            "Ceiling & Walls", "Paint & Wallpaper", "Doors & Door Stops", "Door Locks & Knobs",
            "Flooring", "Lights & Ceiling Fans", "Windows & Screens", "Window Latches",
            "Drapes/Blinds/Shutters", "Plugs & Switches", "Cabinets", "Fireplace", "Other"
        ),
        "Dining Room" to listOf(
            "Ceiling & Walls", "Paint & Wallpaper", "Doors & Door Stops", "Door Locks & Knobs",
            "Flooring", "Lights & Ceiling Fans", "Windows & Screens", "Window Latches",
            "Drapes/Blinds/Shutters", "Plugs & Switches", "Cabinets", "Other"
        ),
        "Kitchen & Breakfast" to listOf(
            "Ceiling & Walls", "Paint & Wallpaper", "Doors & Door Stops", "Door Locks & Knobs",
            "Flooring", "Lights & Ceiling Fans", "Windows & Screens", "Window Latches",
            "Plugs & Switches", "Pantry & Shelves", "Cabinets & Handles", "Drawers & Handles",
            "Countertops", "Range/Cooktop", "Microwave", "Dishwasher", "Oven",
            "Oven Racks & Knobs", "Broiler & Pan", "Vent Hood", "Vent Hood Filter",
            "Garbage Disposal", "Sink & Faucet", "Refrigerator", "Refrigerator Shelves & Drawers",
            "Other"
        ),
        "Halls" to listOf(
            "Ceiling & Walls", "Paint & Wallpaper", "Doors & Door Stops", "Door Locks & Knobs",
            "Flooring", "Light Fixtures", "Plugs & Switches", "Closet Shelves & Rods",
            "Cabinets", "Other"
        ),
        "Family Room" to listOf(
            "Ceiling & Walls", "Paint & Wallpaper", "Doors & Door Stops", "Door Locks & Knobs",
            "Flooring", "Lights & Ceiling Fans", "Windows & Screens", "Window Latches",
            "Drapes/Blinds/Shutters", "Plugs & Switches", "Closet Shelves & Rods", "Cabinets",
            "Fireplace/Logs/Equipment", "Other"
        ),
        "Bedroom" to listOf(
            "Ceiling & Walls", "Paint & Wallpaper", "Doors & Door Stops", "Door Locks & Knobs",
            "Flooring", "Lights & Ceiling Fans", "Windows & Screens", "Window Latches",
            "Drapes/Blinds/Shutters", "Plugs & Switches", "Closet Shelves & Rods", "Cabinets",
            "Other"
        ),
        "Bathroom" to listOf(
            "Ceiling & Walls", "Paint & Wallpaper", "Doors/Locks/Knobs/Stops", "Flooring",
            "Lights & Fans", "Windows & Screens", "Window Latches", "Drapes/Blinds/Shutters",
            "Plugs & Switches", "Closet Shelves & Rods", "Cabinets & Handles", "Countertops",
            "Sinks & Faucets", "Tub/Shower & Faucets", "Toilet/Lid/Seat/Paper Holder",
            "Heaters & Exhaust Fans", "Towel Fixtures", "Other"
        ),
        "Utility Room" to listOf(
            "Ceiling & Walls", "Paint & Wallpaper", "Doors & Door Stops", "Door Locks & Knobs",
            "Flooring", "Light Fixtures", "Plugs & Switches", "Closet Shelves & Rods",
            "Cabinets & Handles", "Countertops", "Sinks & Faucets", "Washer & Dryer",
            "Washer/Dryer Connections", "Other"
        ),
        "Other (Systems)" to listOf(
            "Central A/C & Heat", "Filter", "Thermostat", "Window A/C Units",
            "Space or Wall Heaters", "Water Heater", "Water Softener", "Alarm System",
            "Central Vacuum", "Smoke Detectors", "Other"
        )
    )

    val DEFAULT_SEED_ORDER: List<String> = ITEM_TEMPLATES.keys.toList()

    val DUPLICABLE_TYPES: List<String> = ITEM_TEMPLATES.keys.toList()
}
