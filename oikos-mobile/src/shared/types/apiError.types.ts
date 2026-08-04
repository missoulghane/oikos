export interface ApiErrorBody {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp: string;
}
