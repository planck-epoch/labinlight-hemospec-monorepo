export interface LoginResponse {
    access_token: string;
}

export interface AnalyzeResponse {
    // Define based on what the API returns.
    // Usually NestJS returns the result of the service method.
    // The service returns the saved Analysis object.
    id?: string;
    result?: any;
    // Add other fields as observed/needed
}

export interface HistoryItem {
    id: string;
    date: string;
    // ...
}

class ApiService {
    private baseUrl = import.meta.env.VITE_LABINLIGHT_API_URL || 'https://www.labinlight.dev/api';

    private getHeaders(token?: string) {
        const headers: HeadersInit = {
            'Content-Type': 'application/json',
        };
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        return headers;
    }

    public async login(email: string, password: string): Promise<LoginResponse> {
        const response = await fetch(`${this.baseUrl}/auth/login`, {
            method: 'POST',
            headers: this.getHeaders(),
            body: JSON.stringify({ email, password }),
        });

        if (!response.ok) {
            throw new Error('Login failed');
        }

        return response.json();
    }

    public async analyze(data: any): Promise<AnalyzeResponse> {
        // No auth required for now as per controller comments
        const response = await fetch(`${this.baseUrl}/analyze`, {
            method: 'POST',
            headers: this.getHeaders(),
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            const err = await response.text();
            throw new Error(`Analysis failed: ${err}`);
        }

        return response.json();
    }

    public async getHistory(patientId: string, token: string): Promise<HistoryItem[]> {
        const response = await fetch(`${this.baseUrl}/history/patient/${patientId}`, {
            method: 'GET',
            headers: this.getHeaders(token),
        });

        if (!response.ok) {
            throw new Error('Failed to fetch history');
        }

        return response.json();
    }
}

export const apiService = new ApiService();
