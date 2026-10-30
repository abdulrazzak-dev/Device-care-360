package com.devicecare360.repairguide.config;

import com.devicecare360.repairguide.document.RepairGuide;
import com.devicecare360.repairguide.repository.RepairGuideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RepairGuideDataSeeder implements CommandLineRunner {

    private final RepairGuideRepository repairGuideRepository;

    @Override
    public void run(String... args) {
        try {
            long existingCount = repairGuideRepository.count();
            if (existingCount > 0) {
                log.info("Repair guides already exist in database (count: {}). Skipping initial seed.", existingCount);
                return;
            }

            log.info("Repair guide collection is empty. Seeding official repair manuals...");

            LocalDateTime now = LocalDateTime.now();

            RepairGuide guide1 = RepairGuide.builder()
                    .title("Smartphone Screen & Digitizer Replacement")
                    .category("Smartphone")
                    .brand("Universal / Multi-Brand")
                    .issue("Cracked OLED/LCD glass display, unresponsive touch digitizer, or black screen after impact")
                    .description("Inspect replacement screen assembly, safely open chassis with controlled heat, disconnect internal battery first, detach display flex cables, and test new screen before permanent adhesive sealing.")
                    .riskLevel("MEDIUM")
                    .skillLevel("INTERMEDIATE")
                    .difficulty("INTERMEDIATE")
                    .estimatedTime("45 minutes")
                    .approved(true)
                    .toolsRequired(List.of(
                            "Precision screwdriver set (Pentalobe, Phillips #000, Tri-point Y000)",
                            "Plastic opening picks and nylon spudger",
                            "Suction cup display handle",
                            "Precision curved tweezers",
                            "Heat gun or thermal heating pad (calibrated to 70°C - 80°C)",
                            "Replacement OEM screen assembly with pre-cut perimeter adhesive",
                            "Anti-static ESD wrist strap and mat",
                            "Safety glasses for glass shard protection"
                    ))
                    .safetyWarnings(List.of(
                            "LITHIUM-ION BATTERY HAZARD: Never use metal tools, sharp probes, or excessive leverage near the lithium-ion battery. Puncturing or bending the pouch cell causes immediate thermal runaway, toxic hydrofluoric acid gas, and violent fire.",
                            "TEMPERATURE LIMITS: Do not heat the display assembly above 80°C (176°F). Excessive surface temperature degrades battery chemistry, warps plastic bezels, and permanently damages CMOS camera sensors.",
                            "EYE & HAND PROTECTION: Always wear safety glasses and nitrile gloves. Shattered tempered glass fragments can dislodge under suction tension and cause severe eye injury or lacerations.",
                            "ELECTROSTATIC DISCHARGE (ESD): Wear a grounded anti-static wrist strap. Static charges as low as 100V can silently destroy logic board integrated circuits and display driver ICs."
                    ))
                    .safeInstructions(List.of(
                            "Pre-Repair Diagnostic & Power Down: Power off the smartphone completely. Apply transparent packing tape over the entire cracked screen surface to prevent glass shards from dislodging during extraction.",
                            "Thermal Softening: Place the phone display-face down on a 75°C heating pad or apply a circular heat gun sweep around the outer perimeter for 2 to 3 minutes until warm to the touch (do not exceed 80°C).",
                            "Suction Grip & Initial Seam: Affix the suction handle to the lower third of the glass. Apply steady upward lifting pressure while inserting a thin plastic pick into the lower seam (maximum 2mm depth).",
                            "Perimeter Adhesive Slicing: Work the plastic pick slowly around the perimeter to cut through the waterproof adhesive bead. Keep tool penetration shallow to prevent severing delicate antenna or button flex cables.",
                            "Hinge Open & Bracket Removal: Slowly swing the screen open (refer to manufacturer hinge direction: left or right). Unscrew the metal EMI shielding bracket securing the battery and display ribbon connectors.",
                            "Disconnect Battery FIRST: Immediately disconnect the internal battery flex connector using a non-conductive nylon spudger before touching or unplugging any other component to eliminate electrical short risks.",
                            "Disconnect Display Flex Cables: Gently pry up the display, digitizer, and ambient light sensor/earpiece speaker connectors from their board sockets.",
                            "Pre-Adhesion Dry Test: Connect the new replacement screen connectors, reconnect battery, and power on. Thoroughly test digitizer touch across all screen quadrants, multi-touch pinch-to-zoom, and brightness regulation before peeling adhesive liners.",
                            "Clean Midframe & Install Adhesive: Turn off phone and disconnect battery. Scrape old adhesive residue from the chassis bezel using 99% isopropyl alcohol and cotton swabs. Lay down fresh pre-cut perimeter adhesive gaskets.",
                            "Final Assembly & Sealing: Reconnect display ribbons, reinstall EMI shields with original screws in exact sequence, reconnect battery, seat display flush into midframe, and apply uniform gentle clamping pressure for 15 minutes."
                    ))
                    .steps(List.of(
                            "Pre-Repair Diagnostic & Power Down: Power off the smartphone completely. Apply transparent packing tape over the entire cracked screen surface to prevent glass shards from dislodging during extraction.",
                            "Thermal Softening: Place the phone display-face down on a 75°C heating pad or apply a circular heat gun sweep around the outer perimeter for 2 to 3 minutes until warm to the touch (do not exceed 80°C).",
                            "Suction Grip & Initial Seam: Affix the suction handle to the lower third of the glass. Apply steady upward lifting pressure while inserting a thin plastic pick into the lower seam (maximum 2mm depth).",
                            "Perimeter Adhesive Slicing: Work the plastic pick slowly around the perimeter to cut through the waterproof adhesive bead. Keep tool penetration shallow to prevent severing delicate antenna or button flex cables.",
                            "Hinge Open & Bracket Removal: Slowly swing the screen open (refer to manufacturer hinge direction: left or right). Unscrew the metal EMI shielding bracket securing the battery and display ribbon connectors.",
                            "Disconnect Battery FIRST: Immediately disconnect the internal battery flex connector using a non-conductive nylon spudger before touching or unplugging any other component to eliminate electrical short risks.",
                            "Disconnect Display Flex Cables: Gently pry up the display, digitizer, and ambient light sensor/earpiece speaker connectors from their board sockets.",
                            "Pre-Adhesion Dry Test: Connect the new replacement screen connectors, reconnect battery, and power on. Thoroughly test digitizer touch across all screen quadrants, multi-touch pinch-to-zoom, and brightness regulation before peeling adhesive liners.",
                            "Clean Midframe & Install Adhesive: Turn off phone and disconnect battery. Scrape old adhesive residue from the chassis bezel using 99% isopropyl alcohol and cotton swabs. Lay down fresh pre-cut perimeter adhesive gaskets.",
                            "Final Assembly & Sealing: Reconnect display ribbons, reinstall EMI shields with original screws in exact sequence, reconnect battery, seat display flush into midframe, and apply uniform gentle clamping pressure for 15 minutes."
                    ))
                    .maintenanceTips(List.of(
                            "Install a 9H hardness tempered glass screen protector immediately after display replacement.",
                            "Allow 24 hours of curing before exposing the device to humid environments to ensure maximum water-resistant seal integrity.",
                            "Enable auto-brightness and set screen sleep to 1 minute to prevent permanent OLED burn-in."
                    ))
                    .officialDocumentationUrl("https://www.ifixit.com/Guide")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            RepairGuide guide2 = RepairGuide.builder()
                    .title("Laptop Thermal Throttling & Fan Cleaning")
                    .category("Laptop / PC")
                    .brand("Universal / Multi-Brand")
                    .issue("Excessive fan noise, thermal throttling, sudden thermal shutdowns under load, or high idle temperatures")
                    .description("Remove bottom chassis panel, isolate internal battery power, safely clean heat sink radiator fins and cooling fan blades with compressed air and isopropyl alcohol, and optionally refresh thermal paste.")
                    .riskLevel("LOW")
                    .skillLevel("EASY")
                    .difficulty("EASY")
                    .estimatedTime("30 minutes")
                    .approved(true)
                    .toolsRequired(List.of(
                            "Precision Phillips #0 / Torx screwdriver set",
                            "Plastic spudger or prying pick",
                            "Can of compressed air with directional straw",
                            "Anti-static ESD wrist strap",
                            "99% anhydrous Isopropyl Alcohol (IPA)",
                            "Lint-free microfiber cloth and cotton swabs",
                            "Non-conductive thermal paste (e.g., Arctic MX-4 or Noctua NT-H1)"
                    ))
                    .safetyWarnings(List.of(
                            "BATTERY ISOLATION MANDATORY: Always disconnect the internal battery harness connector immediately upon opening the bottom casing. Accidental conductive tool contact with live motherboard rails can instantly brick the laptop.",
                            "PREVENT FAN OVER-SPINNING & REVERSE-EMF: Mechanically hold fan blades stationary (with a wooden toothpick or finger) while spraying compressed air. High-RPM free spinning acts as an electrical generator that feeds destructive back-EMF voltage directly into motherboard fan headers.",
                            "STATIC ELECTRICITY: Wear a grounded ESD strap attached to unpainted metal. Electrostatic charge transfers can fry sensitive MOSFETs and memory controllers.",
                            "HEATSINK DIAGONAL TORQUE: Loosen and tighten heatsink screws in the stamped numerical diagonal pattern (1 -> 2 -> 3 -> 4) to distribute spring pressure evenly and prevent cracking the bare silicon CPU/GPU dies."
                    ))
                    .safeInstructions(List.of(
                            "Power Isolation & Clean Workspace: Shut down laptop completely, disconnect AC charger, unplug all USB dongles, and place laptop on an anti-static mat.",
                            "Chassis Screw Removal: Back out all underside casing screws. Group screws by length and location to ensure identical placement during reassembly.",
                            "Pry Open Bottom Cover: Insert a plastic prying pick into the seam between the chassis and bottom cover. Work around perimeter clips until the cover lifts off freely.",
                            "Disconnect Internal Battery: Locate the multi-pin battery harness plugged into the motherboard. Slide it straight back using a non-metallic spudger. Press and hold power button for 15 seconds to drain motherboard capacitors.",
                            "Inspect Radiator Vents & Fans: Inspect the centrifugal blower fan and copper fin stack. Note any felt-like lint blockages trapped between fan outlet and heatsink fins.",
                            "Immobilize Fan & Blow Out Dust: Insert a clean toothpick between fan blades to prevent rotation. Apply short, controlled 1-second bursts of compressed air through exhaust fins and intake grills until free of dust.",
                            "Wipe Blades with Isopropyl Alcohol: Lightly moisten a cotton swab with 99% IPA and gently wipe oily dust buildup off individual impeller blades.",
                            "Optional Thermal Repasting: If thermal throttling persists, loosen heatsink screws in reverse diagonal order (4-3-2-1). Lift heatsink vertically. Clean hardened paste from dies with IPA. Place a pea-sized dot of non-conductive paste in die centers.",
                            "Re-seat Heatsink & Torque: Lower heatsink straight down onto dies without sliding. Hand-tighten screws gradually in stamped sequence (1-2-3-4) for even pressure.",
                            "Reconnect Battery & Reassemble: Plug the internal battery harness firmly into its receptacle. Align bottom cover, press perimeter snaps until clicking securely, fasten exterior screws, and power on."
                    ))
                    .steps(List.of(
                            "Power Isolation & Clean Workspace: Shut down laptop completely, disconnect AC charger, unplug all USB dongles, and place laptop on an anti-static mat.",
                            "Chassis Screw Removal: Back out all underside casing screws. Group screws by length and location to ensure identical placement during reassembly.",
                            "Pry Open Bottom Cover: Insert a plastic prying pick into the seam between the chassis and bottom cover. Work around perimeter clips until the cover lifts off freely.",
                            "Disconnect Internal Battery: Locate the multi-pin battery harness plugged into the motherboard. Slide it straight back using a non-metallic spudger. Press and hold power button for 15 seconds to drain motherboard capacitors.",
                            "Inspect Radiator Vents & Fans: Inspect the centrifugal blower fan and copper fin stack. Note any felt-like lint blockages trapped between fan outlet and heatsink fins.",
                            "Immobilize Fan & Blow Out Dust: Insert a clean toothpick between fan blades to prevent rotation. Apply short, controlled 1-second bursts of compressed air through exhaust fins and intake grills until free of dust.",
                            "Wipe Blades with Isopropyl Alcohol: Lightly moisten a cotton swab with 99% IPA and gently wipe oily dust buildup off individual impeller blades.",
                            "Optional Thermal Repasting: If thermal throttling persists, loosen heatsink screws in reverse diagonal order (4-3-2-1). Lift heatsink vertically. Clean hardened paste from dies with IPA. Place a pea-sized dot of non-conductive paste in die centers.",
                            "Re-seat Heatsink & Torque: Lower heatsink straight down onto dies without sliding. Hand-tighten screws gradually in stamped sequence (1-2-3-4) for even pressure.",
                            "Reconnect Battery & Reassemble: Plug the internal battery harness firmly into its receptacle. Align bottom cover, press perimeter snaps until clicking securely, fasten exterior screws, and power on."
                    ))
                    .maintenanceTips(List.of(
                            "Never operate laptops on soft fabrics, bedspreads, or carpets that block intake grilles and accelerate dust intake.",
                            "Elevate the laptop rear feet by 15mm using a riser stand to increase cool intake airflow by up to 25%.",
                            "Schedule preventive fan cleaning every 6 months to maintain optimal boost clocks and battery endurance."
                    ))
                    .officialDocumentationUrl("https://www.ifixit.com/Guide")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            RepairGuide guide3 = RepairGuide.builder()
                    .title("Inverter Split AC Water Drainage Leak Fix")
                    .category("Air Conditioner")
                    .brand("Universal / Multi-Brand")
                    .issue("Indoor unit dripping water down the wall, gurgling sound in drain line, or stale moisture odor")
                    .description("Isolate main breaker power, open indoor evaporator housing, inspect condensate drip trough, flush gelatinous algae plugs with vinegar solution, and verify continuous downward gravity slope.")
                    .riskLevel("LOW")
                    .skillLevel("BEGINNER")
                    .difficulty("BEGINNER")
                    .estimatedTime("25 minutes")
                    .approved(true)
                    .toolsRequired(List.of(
                            "Stepped drain hose adapter or flexible clear vinyl tubing",
                            "Distilled white vinegar (5% acidity) or HVAC condensate drain cleaner",
                            "Condensate squeeze bottle or manual hand siphon pump",
                            "Wet/Dry utility shop vacuum with seal rag",
                            "Bucket and microfiber cleaning towels",
                            "Phillips #2 screwdriver",
                            "Protective rubber gloves and safety glasses"
                    ))
                    .safetyWarnings(List.of(
                            "MAINS ELECTRICAL ISOLATION: Switch off the dedicated air conditioning circuit breaker at the main distribution board before touching any indoor or outdoor unit parts.",
                            "RAZOR-SHARP EVAPORATOR FINS: The aluminum heat exchanger coils behind filters have paper-thin razor edges. Wear puncture-resistant gloves and avoid brushing skin against coil surfaces.",
                            "REFRIGERANT LINE INTEGRITY: Never hang, bend, or pull against insulated copper tubing. Cracking pressurized refrigerant lines causes hazardous gas leaks and expensive compressor burnouts."
                    ))
                    .safeInstructions(List.of(
                            "Power Isolation: Flip off the AC unit circuit breaker at the main electrical panel. Confirm that unit LED lights and indoor display are dark.",
                            "Open Front Panel & Remove Filters: Gently lift the front intake grille until it locks open. Slide out both primary mesh filters and place them in a wash basin.",
                            "Inspect Condensate Drip Trough: Look beneath the evaporator coil bank into the plastic condensate drip channel. Check if accumulated water is stagnant and filled with jelly-like biofilm sludge.",
                            "Vacuum Exterior Drain Outlet: Locate the external gravity drain exit pipe outside the structure. Wrap a damp cloth around the pipe opening, connect a wet/dry vacuum nozzle, and run for 60 seconds to pull out clogged algae plugs.",
                            "Access Indoor Drain Connection: If exterior pipe is inaccessible, unfasten lower decorative screws on the indoor unit casing and gently pivot the lower housing forward to reveal the drain pan elbow.",
                            "Prepare Vinegar Flush Solution: Fill a squeeze bottle with a 50/50 mixture of warm water and distilled white vinegar (or specialized HVAC pan treatment tablets).",
                            "Flush Drain Pan & Hose: Slowly inject the vinegar solution directly into the indoor drain channel. The acetic acid dissolves mold mats, bacterial slime, and calcium scale without damaging PVC.",
                            "Inspect Gravity Pitch: Check that the flexible drain pipe maintains a continuous downward slope of at least 1cm per meter without sags, dips, or upward kinks that create standing airlocks.",
                            "Wash Mesh Filters: Rinse dust filters with lukewarm water and mild detergent. Shake off excess droplets and allow to dry fully in the shade before reinstalling.",
                            "Water Test & Power Restore: Pour 500ml of clean water into the drain trough and verify instantaneous, leak-free drainage outside. Close casing, restore breaker power, and test run in Cool mode."
                    ))
                    .steps(List.of(
                            "Power Isolation: Flip off the AC unit circuit breaker at the main electrical panel. Confirm that unit LED lights and indoor display are dark.",
                            "Open Front Panel & Remove Filters: Gently lift the front intake grille until it locks open. Slide out both primary mesh filters and place them in a wash basin.",
                            "Inspect Condensate Drip Trough: Look beneath the evaporator coil bank into the plastic condensate drip channel. Check if accumulated water is stagnant and filled with jelly-like biofilm sludge.",
                            "Vacuum Exterior Drain Outlet: Locate the external gravity drain exit pipe outside the structure. Wrap a damp cloth around the pipe opening, connect a wet/dry vacuum nozzle, and run for 60 seconds to pull out clogged algae plugs.",
                            "Access Indoor Drain Connection: If exterior pipe is inaccessible, unfasten lower decorative screws on the indoor unit casing and gently pivot the lower housing forward to reveal the drain pan elbow.",
                            "Prepare Vinegar Flush Solution: Fill a squeeze bottle with a 50/50 mixture of warm water and distilled white vinegar (or specialized HVAC pan treatment tablets).",
                            "Flush Drain Pan & Hose: Slowly inject the vinegar solution directly into the indoor drain channel. The acetic acid dissolves mold mats, bacterial slime, and calcium scale without damaging PVC.",
                            "Inspect Gravity Pitch: Check that the flexible drain pipe maintains a continuous downward slope of at least 1cm per meter without sags, dips, or upward kinks that create standing airlocks.",
                            "Wash Mesh Filters: Rinse dust filters with lukewarm water and mild detergent. Shake off excess droplets and allow to dry fully in the shade before reinstalling.",
                            "Water Test & Power Restore: Pour 500ml of clean water into the drain trough and verify instantaneous, leak-free drainage outside. Close casing, restore breaker power, and test run in Cool mode."
                    ))
                    .maintenanceTips(List.of(
                            "Pour 250ml of white vinegar into the indoor drain trough at the start and end of every summer cooling season.",
                            "Clean mesh intake filters every 2 to 3 weeks during heavy summer operation to prevent airborne lint from entering the wet drain pan.",
                            "Ensure the exterior drain hose tip hangs suspended in free air and is never submerged in soil, puddles, or plant pots."
                    ))
                    .officialDocumentationUrl("https://www.energystar.gov/products/heating_cooling")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            RepairGuide guide4 = RepairGuide.builder()
                    .title("Washing Machine Drain Pump Unclogging & OE Error Reset")
                    .category("Washing Machine")
                    .brand("Universal / Multi-Brand")
                    .issue("Washing machine failing to drain, tub full of stagnant water, loud humming noise during drain cycle, or OE / 5E / E20 error code")
                    .description("Safely unplug appliance, access lower service flap, drain standing drum water via emergency drain tube, extract debris filter, clear coins and lint from pump impeller, and reseat rubber gasket.")
                    .riskLevel("MEDIUM")
                    .skillLevel("EASY")
                    .difficulty("EASY")
                    .estimatedTime("30 minutes")
                    .approved(true)
                    .toolsRequired(List.of(
                            "Shallow baking sheet, cookie tray, or low-profile drain container",
                            "Stack of heavy absorbent towels",
                            "Needle-nose pliers",
                            "High-intensity LED flashlight",
                            "Plastic bristle brush",
                            "Rubber waterproof gloves"
                    ))
                    .safetyWarnings(List.of(
                            "ELECTRICAL SHOCK HAZARD: Unplug the washing machine line cord from the mains power socket before touching any access doors. Never interact with internal mechanical parts while the machine is connected to live power.",
                            "SCALDING WATER RISK: If the cycle stalled during a hot water wash or sanitize cycle (60°C - 90°C), allow at least 45 minutes for the water to cool to room temperature before releasing the drain hose.",
                            "RUBBER GASKET WATERPROOF SEAL: Ensure the black circular O-ring seal on the filter cap is clean, intact, and hand-tightened firmly. Cross-threading or trapped debris on the gasket will cause catastrophic floor flooding during spin cycles."
                    ))
                    .safeInstructions(List.of(
                            "Disconnect Mains Power: Unplug the washing machine from the wall electrical socket. Lay absorbent towels directly in front of the lower bottom panel.",
                            "Open Filter Access Door: Press the lower service door latch (usually located on bottom-right of front facade) using a coin or flat screwdriver to swing it open.",
                            "Deploy Emergency Drain Hose: Locate the small black emergency drain tube positioned next to the round filter plug. Pull it forward, place the tip in a shallow tray, remove the stopper, and drain water in batches into a bucket.",
                            "Drain Remaining Water: Re-insert the emergency tube stopper firmly and clip the hose back into its storage notch. Position the shallow tray under the main circular filter plug.",
                            "Unscrew Main Debris Filter: Slowly turn the round filter knob counter-clockwise a quarter turn. Allow residual water to trickle into tray, then unscrew completely and slide the filter assembly out.",
                            "Remove Debris & Foreign Objects: Clear trapped coins, safety pins, hair clips, lint balls, and garment underwires from the filter basket. Scrub under warm running tap water.",
                            "Inspect Pump Chamber & Impeller: Shine a flashlight into the interior pump housing. Locate the plastic impeller fan blades at the back. Spin the impeller with a gloved finger; it should rotate with slight spring-like magnetic steps without binding.",
                            "Clean Housing & Check O-Ring: Wipe the internal pump cavity and threads with a microfiber cloth. Inspect the rubber sealing O-ring for nicks, tears, or grit.",
                            "Reinstall Filter Squarely: Insert filter straight into housing and screw clockwise until tight and the indicator aligns with the vertical stop marker. Do not over-torque with tools.",
                            "Test Run & Reset OE Code: Close access door, reconnect electrical power cord, select a short 'Rinse & Spin' or 'Drain' cycle, and verify that drum water evacuates within 60 seconds with no error codes."
                    ))
                    .steps(List.of(
                            "Disconnect Mains Power: Unplug the washing machine from the wall electrical socket. Lay absorbent towels directly in front of the lower bottom panel.",
                            "Open Filter Access Door: Press the lower service door latch (usually located on bottom-right of front facade) using a coin or flat screwdriver to swing it open.",
                            "Deploy Emergency Drain Hose: Locate the small black emergency drain tube positioned next to the round filter plug. Pull it forward, place the tip in a shallow tray, remove the stopper, and drain water in batches into a bucket.",
                            "Drain Remaining Water: Re-insert the emergency tube stopper firmly and clip the hose back into its storage notch. Position the shallow tray under the main circular filter plug.",
                            "Unscrew Main Debris Filter: Slowly turn the round filter knob counter-clockwise a quarter turn. Allow residual water to trickle into tray, then unscrew completely and slide the filter assembly out.",
                            "Remove Debris & Foreign Objects: Clear trapped coins, safety pins, hair clips, lint balls, and garment underwires from the filter basket. Scrub under warm running tap water.",
                            "Inspect Pump Chamber & Impeller: Shine a flashlight into the interior pump housing. Locate the plastic impeller fan blades at the back. Spin the impeller with a gloved finger; it should rotate with slight spring-like magnetic steps without binding.",
                            "Clean Housing & Check O-Ring: Wipe the internal pump cavity and threads with a microfiber cloth. Inspect the rubber sealing O-ring for nicks, tears, or grit.",
                            "Reinstall Filter Squarely: Insert filter straight into housing and screw clockwise until tight and the indicator aligns with the vertical stop marker. Do not over-torque with tools.",
                            "Test Run & Reset OE Code: Close access door, reconnect electrical power cord, select a short 'Rinse & Spin' or 'Drain' cycle, and verify that drum water evacuates within 60 seconds with no error codes."
                    ))
                    .maintenanceTips(List.of(
                            "Always empty pockets (coins, keys, hairpins) before placing clothing into the washing machine.",
                            "Unscrew and rinse the pump debris filter once every 60 to 90 days to prevent lint compaction and motor strain.",
                            "Use zippered mesh laundry bags when washing small items like baby socks, laces, and delicates."
                    ))
                    .officialDocumentationUrl("https://www.ifixit.com/Guide")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            RepairGuide guide5 = RepairGuide.builder()
                    .title("Refrigerator Frost Buildup & Defrost Drain Line Clearing")
                    .category("Refrigerator")
                    .brand("Universal / Multi-Brand")
                    .issue("Sheet of ice forming on freezer floor, water leaking under vegetable crisper drawers, or abnormal humming fan noise from frost buildup")
                    .description("Transfer perishables to cooler, unplug refrigerator, remove rear freezer interior panel to expose evaporator coils, defrost drain trough with warm water, and flush drain hole until clear.")
                    .riskLevel("MEDIUM")
                    .skillLevel("INTERMEDIATE")
                    .difficulty("INTERMEDIATE")
                    .estimatedTime("40 minutes")
                    .approved(true)
                    .toolsRequired(List.of(
                            "Warm water (45°C - 55°C) mixed with 1 tablespoon baking soda",
                            "Bulb syringe or turkey baster with 30cm flexible vinyl tube",
                            "Phillips #2 screwdriver",
                            "Plastic scraper or wooden spatula (NEVER metal)",
                            "Absorbent towels and shallow catch tray",
                            "Hair dryer (set to LOW/WARM heat only, NEVER high or heat gun)",
                            "Thermal cooler for temporary perishable storage"
                    ))
                    .safetyWarnings(List.of(
                            "NEVER PUNCTURE EVAPORATOR COILS: Never use knives, ice picks, screwdrivers, or sharp metal scrapers to chip ice from evaporator coils. Puncturing thin aluminum tubes releases refrigerant gas under pressure, completely ruins the appliance, and creates a costly hazardous repair.",
                            "PLASTIC LINER MELT HAZARD: Never use high-temperature industrial heat guns. Excessive heat warps and melts the polystyrene interior liner within seconds.",
                            "ELECTRICAL SHOCK & DEFROST HEATER: Unplug the refrigerator before removing rear internal panels. Defrost heaters operate at high current and can exceed 200°C when activated."
                    ))
                    .safeInstructions(List.of(
                            "Food Relocation & Power Isolation: Unplug the refrigerator power cord from the wall outlet. Move frozen food and perishables into insulated cooler bags with ice packs.",
                            "Remove Freezer Shelves & Bins: Empty the freezer compartment completely. Slide out wire racks, ice buckets, and bottom drawer bins.",
                            "Remove Rear Evaporator Panel: Unfasten the Phillips retaining screws securing the interior rear plastic panel. Carefully ease the panel forward, uncoupling the evaporator fan wiring harness connector.",
                            "Inspect Evaporator & Drain Trough: Examine the aluminum coil stack. Note whether frost is uniformly thin or caked in solid ice. Look down at the V-shaped metal drain trough located directly beneath the coils.",
                            "Defrost Trough with Controlled Heat: Use a hair dryer on LOW/WARM heat held at least 20cm away (moving continuously) or pour warm water (45°C-55°C, NOT boiling) into the trough to melt the ice block.",
                            "Locate Defrost Drain Hole: Mop up melted water with towels to expose the 1/2-inch drain orifice at the bottom center of the trough.",
                            "Flush Drain Line with Syringe: Fill a bulb syringe with warm water and baking soda. Press the flexible vinyl tube into the drain hole and inject with firm pressure. Repeat 4 to 6 times until water drains freely with an audible splash into the rear drain pan.",
                            "Inspect Rear Compressor Evaporator Pan: Move to the rear exterior of the refrigerator. Remove the lower cardboard grille and confirm that warm flushed water arrived in the plastic condensation pan above the compressor.",
                            "Clean Duckbill Drain Valve: If the rear rubber drain tube has a pinch or duckbill valve, pull it off, rinse out sticky mineral and dust clogs under running water, and push it back onto the tube.",
                            "Reassemble & Restore Power: Reconnect the evaporator fan harness, screw the rear interior panel back into place, restore shelves and food, plug the refrigerator in, and verify whisper-quiet fan operation."
                    ))
                    .steps(List.of(
                            "Food Relocation & Power Isolation: Unplug the refrigerator power cord from the wall outlet. Move frozen food and perishables into insulated cooler bags with ice packs.",
                            "Remove Freezer Shelves & Bins: Empty the freezer compartment completely. Slide out wire racks, ice buckets, and bottom drawer bins.",
                            "Remove Rear Evaporator Panel: Unfasten the Phillips retaining screws securing the interior rear plastic panel. Carefully ease the panel forward, uncoupling the evaporator fan wiring harness connector.",
                            "Inspect Evaporator & Drain Trough: Examine the aluminum coil stack. Note whether frost is uniformly thin or caked in solid ice. Look down at the V-shaped metal drain trough located directly beneath the coils.",
                            "Defrost Trough with Controlled Heat: Use a hair dryer on LOW/WARM heat held at least 20cm away (moving continuously) or pour warm water (45°C-55°C, NOT boiling) into the trough to melt the ice block.",
                            "Locate Defrost Drain Hole: Mop up melted water with towels to expose the 1/2-inch drain orifice at the bottom center of the trough.",
                            "Flush Drain Line with Syringe: Fill a bulb syringe with warm water and baking soda. Press the flexible vinyl tube into the drain hole and inject with firm pressure. Repeat 4 to 6 times until water drains freely with an audible splash into the rear drain pan.",
                            "Inspect Rear Compressor Evaporator Pan: Move to the rear exterior of the refrigerator. Remove the lower cardboard grille and confirm that warm flushed water arrived in the plastic condensation pan above the compressor.",
                            "Clean Duckbill Drain Valve: If the rear rubber drain tube has a pinch or duckbill valve, pull it off, rinse out sticky mineral and dust clogs under running water, and push it back onto the tube.",
                            "Reassemble & Restore Power: Reconnect the evaporator fan harness, screw the rear interior panel back into place, restore shelves and food, plug the refrigerator in, and verify whisper-quiet fan operation."
                    ))
                    .maintenanceTips(List.of(
                            "Perform the 'dollar bill test' on door gaskets: close the door over a bill; if it slides out without drag, replace magnetic seal gaskets.",
                            "Never store warm or uncovered liquid containers in the freezer to minimize excess frost crystallization.",
                            "Vacuum dust and pet hair from the rear/bottom compressor coils every 6 months to maintain heat dissipation efficiency."
                    ))
                    .officialDocumentationUrl("https://www.energystar.gov/products/appliances/refrigerators")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            List<RepairGuide> seedGuides = List.of(guide1, guide2, guide3, guide4, guide5);
            repairGuideRepository.saveAll(seedGuides);
            log.info("Successfully seeded {} official repair guides into database.", seedGuides.size());

        } catch (Exception e) {
            log.error("Failed to seed repair guides into MongoDB. Database connection or insertion error: {}", e.getMessage(), e);
        }
    }
}
