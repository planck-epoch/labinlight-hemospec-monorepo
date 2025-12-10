import { apiService } from './ApiService';

export interface User {
    id: string;
    email: string;
    role: 'doctor' | 'nurse' | 'admin';
    name: string;
}

class AuthService {
    private currentUser: User | null = null;
    private token: string | null = null;

    public async login(email: string, password: string): Promise<User> {
        try {
            const response = await apiService.login(email, password);
            this.token = response.access_token;
            apiService.setToken(this.token);

            // For now, mock the user details as the API only returns token
            this.currentUser = {
                id: '2',
                email: email,
                role: 'doctor',
                name: email.includes('daniel') ? 'Daniel Sousa' : 'User'
            };
            return this.currentUser;
        } catch (error) {
            console.error('Login failed', error);
            throw error;
        }
    }

    public async logout(): Promise<void> {
        this.currentUser = null;
        this.token = null;
        apiService.setToken(null);
    }

    public getUser(): User | null {
        return this.currentUser;
    }

    public getToken(): string | null {
        return this.token;
    }

    public isAuthenticated(): boolean {
        return !!this.currentUser;
    }
}

export const authService = new AuthService();
