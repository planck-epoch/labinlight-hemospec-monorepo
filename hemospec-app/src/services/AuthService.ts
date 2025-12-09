export interface User {
    id: string;
    email: string;
    role: 'doctor' | 'nurse' | 'admin';
    name: string;
}

class AuthService {
    private currentUser: User | null = null;

    public async login(email: string, password: string): Promise<User> {
        // Hardcoded login for testing
        if (email === 'daniel.sousa@labinlight.com' && password === 'secret') {
            this.currentUser = {
                id: '1',
                email: 'daniel.sousa@labinlight.com',
                role: 'doctor',
                name: 'Daniel Sousa'
            };
            return this.currentUser;
        }

        throw new Error('Invalid credentials');
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
