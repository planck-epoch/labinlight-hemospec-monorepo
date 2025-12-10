FROM node:20-alpine

WORKDIR /app

# --- Add this line here ---
RUN corepack enable && corepack prepare pnpm@latest --activate

# Install dependencies first for caching
COPY package*.json ./
RUN pnpm install

# Copy source and build
COPY . .
RUN pnpm build

EXPOSE 3000
CMD ["node", "dist/main"]