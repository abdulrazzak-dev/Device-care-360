/**
 * DeviceCare 360 Canonical Specializations and Category Utilities.
 * Synchronized with backend DeviceSpecialization enum.
 */

export const CANONICAL_SPECIALIZATIONS = [
  {
    key: 'SMARTPHONE_MOBILE',
    label: 'Smartphone / Mobile',
    description: 'Smartphones, mobile phones, tablet screen & battery repairs',
    icon: 'Smartphone'
  },
  {
    key: 'LAPTOP_DESKTOP_PC',
    label: 'Laptop / Desktop PC',
    description: 'Laptops, desktop computers, motherboards, data recovery',
    icon: 'Laptop'
  },
  {
    key: 'REFRIGERATOR',
    label: 'Refrigerator',
    description: 'Domestic & commercial fridges, compressors, cooling systems',
    icon: 'Refrigerator'
  },
  {
    key: 'AIR_CONDITIONER',
    label: 'Air Conditioner (AC)',
    description: 'Inverter AC units, split units, gas filling, thermostat repair',
    icon: 'Wind'
  },
  {
    key: 'SMART_TV_AUDIO',
    label: 'Smart TV / Audio',
    description: 'LED/OLED Smart TVs, home theater systems, audio receivers',
    icon: 'Tv'
  },
  {
    key: 'WASHING_MACHINE',
    label: 'Washing Machine',
    description: 'Front load, top load, dryers, drum motors, drainage valves',
    icon: 'WashingMachine'
  },
  {
    key: 'MICROWAVE_KITCHEN_APPLIANCES',
    label: 'Microwave / Kitchen Appliances',
    description: 'Microwave ovens, blenders, induction stoves, smart appliances',
    icon: 'Utensils'
  }
];

/**
 * Normalizes any category string, device name, or keyword to its canonical enum key.
 */
export const normalizeToCanonical = (input) => {
  if (!input) return null;
  const raw = String(input).trim().toUpperCase();

  // Exact match with canonical key
  const exact = CANONICAL_SPECIALIZATIONS.find(s => s.key === raw);
  if (exact) return exact.key;

  // Keyword match
  if (raw.includes('SMARTPHONE') || raw.includes('MOBILE') || raw.includes('PHONE')) {
    return 'SMARTPHONE_MOBILE';
  }
  if (raw.includes('LAPTOP') || raw.includes('DESKTOP') || raw.includes('PC') || raw.includes('COMPUTER')) {
    return 'LAPTOP_DESKTOP_PC';
  }
  if (raw.includes('REFRIGERATOR') || raw.includes('FRIDGE')) {
    return 'REFRIGERATOR';
  }
  if (raw.includes('AIR CONDITIONER') || raw.includes('AC') || raw.includes('A/C') || raw.includes('AIR_CONDITIONER')) {
    return 'AIR_CONDITIONER';
  }
  if (raw.includes('TELEVISION') || raw.includes('TV') || raw.includes('AUDIO') || raw.includes('SOUND')) {
    return 'SMART_TV_AUDIO';
  }
  if (raw.includes('WASHING') || raw.includes('WASHER') || raw.includes('LAUNDRY')) {
    return 'WASHING_MACHINE';
  }
  if (raw.includes('MICROWAVE') || raw.includes('KITCHEN') || raw.includes('OVEN')) {
    return 'MICROWAVE_KITCHEN_APPLIANCES';
  }

  return null;
};

/**
 * Gets human-readable display label for a category (canonical or raw).
 */
export const getCategoryDisplayLabel = (category) => {
  if (!category) return 'General Repair';
  const canonical = normalizeToCanonical(category);
  const found = CANONICAL_SPECIALIZATIONS.find(s => s.key === canonical);
  return found ? found.label : category;
};

/**
 * Determines whether a technician's specializations list includes the target category.
 */
export const matchesCategory = (technicianSpecializations, targetCategory) => {
  if (!Array.isArray(technicianSpecializations) || !targetCategory) return false;
  const targetCanonical = normalizeToCanonical(targetCategory);

  return technicianSpecializations.some(spec => {
    if (!spec) return false;
    const specCanonical = normalizeToCanonical(spec);
    if (targetCanonical && specCanonical && targetCanonical === specCanonical) {
      return true;
    }
    return String(spec).trim().toLowerCase() === String(targetCategory).trim().toLowerCase();
  });
};
