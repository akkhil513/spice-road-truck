const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'https://api.spiceroadtruck.com';

export interface Dish {
  id?: number;
  name: string;
  description: string;
  price: number;
  category: string;
  imageUrl?: string;
}

export async function getAllDishes(): Promise<Dish[]> {
  const res = await fetch(`${API_BASE}/srt/v1/dishes`, { cache: 'no-store' });
  if (!res.ok) throw new Error('Failed to fetch dishes');
  return res.json();
}

export async function getDishesByCategory(category: string): Promise<Dish[]> {
  const res = await fetch(`${API_BASE}/srt/v1/dishes?category=${encodeURIComponent(category)}`, { cache: 'no-store' });
  if (!res.ok) throw new Error('Failed to fetch dishes');
  return res.json();
}

export async function getDishById(id: number): Promise<Dish> {
  const res = await fetch(`${API_BASE}/srt/v1/dishes/${id}`, { cache: 'no-store' });
  if (!res.ok) throw new Error('Failed to fetch dish');
  return res.json();
}

export async function createDish(dish: Dish): Promise<Dish> {
  const res = await fetch(`${API_BASE}/srt/v1/dishes`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dish),
  });
  if (!res.ok) throw new Error('Failed to create dish');
  return res.json();
}

export async function updateDish(id: number, dish: Dish): Promise<Dish> {
  const res = await fetch(`${API_BASE}/srt/v1/dishes/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dish),
  });
  if (!res.ok) throw new Error('Failed to update dish');
  return res.json();
}

export async function deleteDish(id: number): Promise<boolean> {
  const res = await fetch(`${API_BASE}/srt/v1/dishes/${id}`, {
    method: 'DELETE',
  });
  if (!res.ok) throw new Error('Failed to delete dish');
  return true;
}