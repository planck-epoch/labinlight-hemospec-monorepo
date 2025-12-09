import { Injectable } from '@nestjs/common';
import * as bcrypt from 'bcrypt';
import { User } from './entities/user.entity';

@Injectable()
export class UsersService {
  // This is a mock user store. Replace with your database repository.
  private readonly users: User[] = [
    {
      id: 1,
      email: 'test@example.com',
      passwordHash: bcrypt.hashSync('password123', 10), // Store hashed passwords
    },
  ];

  async findOneByEmail(email: string): Promise<User | undefined> {
    return this.users.find((user) => user.email === email);
  }

  // Add other user-related methods as needed (e.g., create, findById).
}
