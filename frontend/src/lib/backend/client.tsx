import { paths } from "@/lib/backend/apiV1/schema";
import createClient from "openapi-fetch";

const clientWithNoHeaders = createClient<paths>({
  baseUrl: "http://localhost:8080",
});

const client = createClient<paths>({
  baseUrl: "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
  credentials: "include",
});

export { client, clientWithNoHeaders };
