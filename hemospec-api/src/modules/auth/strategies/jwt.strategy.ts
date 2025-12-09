import { Injectable } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor(private readonly configService: ConfigService) {
    super({
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      ignoreExpiration: false,
      // Use getOrThrow to ensure the secret is always a string.
      secretOrKey: configService.getOrThrow<string>('app.jwtSecret'),
    });
  }

  async validate(payload: any) {
    // The payload is the decoded JWT. You can use it to attach user info to the request.
    return { userId: payload.sub, email: payload.email };
  }
}
