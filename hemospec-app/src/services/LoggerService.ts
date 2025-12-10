
export type LogLevel = 'info' | 'warn' | 'error' | 'debug';

export interface LogEntry {
  timestamp: string;
  level: LogLevel;
  message: string;
  data?: any;
}

class LoggerService {
  private logs: LogEntry[] = [];
  private listeners: (() => void)[] = [];
  private MAX_LOGS = 500;

  private notify() {
    this.listeners.forEach(l => l());
  }

  public subscribe(listener: () => void) {
    this.listeners.push(listener);
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  private addLog(level: LogLevel, message: string, data?: any) {
    const entry: LogEntry = {
      timestamp: new Date().toISOString(),
      level,
      message,
      data
    };

    // Add to internal store (Newest first)
    this.logs.unshift(entry);
    if (this.logs.length > this.MAX_LOGS) {
        this.logs.pop();
    }

    // Console output for Logcat/Web Console
    const prefix = '[HemoSpec]';
    // Map 'debug' to 'log' or 'debug' depending on platform preference, usually 'log' is safer for visibility
    const consoleMethod = level === 'debug' ? 'log' : level;

    if (data) {
        console[consoleMethod](prefix, message, data);
    } else {
        console[consoleMethod](prefix, message);
    }

    this.notify();
  }

  log(message: string, data?: any) { this.addLog('info', message, data); }
  info(message: string, data?: any) { this.addLog('info', message, data); }
  warn(message: string, data?: any) { this.addLog('warn', message, data); }
  error(message: string, data?: any) { this.addLog('error', message, data); }
  debug(message: string, data?: any) { this.addLog('debug', message, data); }

  getLogs() { return this.logs; }
  clear() {
      this.logs = [];
      this.notify();
  }
}

export const logger = new LoggerService();
