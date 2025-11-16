// API Client for Global Airline League

export interface HealthResponse {
  status: string;
  version: string;
}

class ApiClient {
  private baseUrl: string;

  constructor() {
    this.baseUrl = '/api';
  }

  async getHealth(): Promise<HealthResponse> {
    const response = await fetch(`${this.baseUrl}/health`);
    if (!response.ok) {
      throw new Error(`Health check failed: ${response.statusText}`);
    }
    return response.json();
  }

  async getAirlines(): Promise<any[]> {
    const response = await fetch(`${this.baseUrl}/airlines`);
    if (!response.ok) {
      throw new Error(`Failed to fetch airlines: ${response.statusText}`);
    }
    return response.json();
  }

  async getAirports(): Promise<any[]> {
    const response = await fetch(`${this.baseUrl}/airports`);
    if (!response.ok) {
      throw new Error(`Failed to fetch airports: ${response.statusText}`);
    }
    return response.json();
  }

  async getRoutes(): Promise<any[]> {
    const response = await fetch(`${this.baseUrl}/routes`);
    if (!response.ok) {
      throw new Error(`Failed to fetch routes: ${response.statusText}`);
    }
    return response.json();
  }

  async search(query: string): Promise<any[]> {
    const response = await fetch(`${this.baseUrl}/search?q=${encodeURIComponent(query)}`);
    if (!response.ok) {
      throw new Error(`Search failed: ${response.statusText}`);
    }
    return response.json();
  }
}

export const apiClient = new ApiClient();
