export interface User {
    id: string;
    email: string;
    role: 'doctor' | 'nurse' | 'admin';
    name: string;
}

class AuthService {
    private currentUser: User | null = null;

    public async login(email: string, password: string): Promise<User> {
        // Mock login
        await new Promise(resolve => setTimeout(resolve, 1000));

        if (password === 'error') {
            throw new Error('Invalid credentials');
        }

        this.currentUser = {
            id: '1',
            email,
            role: 'doctor',
            name: 'Dr. Silva'
        };

        return this.currentUser;
    }

    public async logout(): Promise<void> {
        this.currentUser = null;
    }

    public getUser(): User | null {
        return this.currentUser;
    }

    public isAuthenticated(): boolean {
        return !!this.currentUser;
    }
}

export const authService = new AuthService();
