import toast from "react-hot-toast";
import { ApiError } from "@/api/client";

export function toastApiError(error: unknown): void {
  if (error instanceof ApiError) {
    toast.error(error.message);

    if (error.fieldErrors) {
      Object.entries(error.fieldErrors).forEach(([field, message]) => {
        toast.error(`${field}: ${message}`);
      });
    }
    return;
  }

  if (error instanceof Error) {
    toast.error(error.message);
    return;
  }

  toast.error("An unexpected error occurred.");
}
