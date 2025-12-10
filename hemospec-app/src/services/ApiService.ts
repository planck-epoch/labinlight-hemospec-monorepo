import { logger } from './LoggerService';

export interface LoginResponse {
    access_token: string;
}

export interface AnalyzeResponse {
    id?: string;
    result?: any;
    [key: string]: any;
}

export interface HistoryItem {
    id: string;
    date: string;
    [key: string]: any;
}

class ApiService {
    private baseUrl = 'https://labinlight.dev/api';
    private accessToken: string | null = null;

    public setToken(token: string | null) {
        this.accessToken = token;
    }

    private getHeaders(token?: string) {
        const headers: HeadersInit = {
            'Content-Type': 'application/json',
        };
        const authToken = token || this.accessToken;
        if (authToken) {
            headers['Authorization'] = `Bearer ${authToken}`;
        }
        return headers;
    }

    private async request<T>(url: string, options: RequestInit): Promise<T> {
        const fullUrl = `${this.baseUrl}${url}`;

        logger.info(`API Request: ${options.method} ${url}`, {
            url: fullUrl,
            method: options.method,
            body: options.body ? JSON.parse(options.body as string) : undefined
        });

        try {
            const response = await fetch(fullUrl, options);

            let data: any;
            const contentType = response.headers.get("content-type");

            if (contentType && contentType.indexOf("application/json") !== -1) {
                data = await response.json();
            } else {
                data = await response.text();
            }

            if (!response.ok) {
                logger.error(`API Error: ${response.status} ${url}`, {
                    status: response.status,
                    statusText: response.statusText,
                    body: data
                });
                throw new Error(typeof data === 'string' ? data : JSON.stringify(data) || response.statusText);
            }

            logger.info(`API Response: ${response.status} ${url}`, {
                status: response.status,
                body: data
            });

            return data as T;
        } catch (error: any) {
            logger.error(`API Network Error: ${url}`, error);
            throw error;
        }
    }

    public async login(email: string, password: string): Promise<LoginResponse> {
        return this.request<LoginResponse>('/auth/login', {
            method: 'POST',
            headers: this.getHeaders(),
            body: JSON.stringify({ email, password }),
        });
    }

    public async analyze(data: any): Promise<AnalyzeResponse> {
        // Log the exact payload for verification
        console.log('Analyze Payload:', JSON.stringify(data));
        return this.request<AnalyzeResponse>('/analyze', {
            method: 'POST',
            headers: this.getHeaders(),
            body: JSON.stringify(data),
        });
    }

    public async getHistory(patientId: string, token?: string): Promise<HistoryItem[]> {
        return this.request<HistoryItem[]>(`/history/patient/${patientId}`, {
            method: 'GET',
            headers: this.getHeaders(token),
        });
    }
}

export const apiService = new ApiService();
