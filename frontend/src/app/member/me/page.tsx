import RequireAuthenticated from "@/components/auth/RequireAuthenticated";
import ClientPage from "./ClientPage";

export default async function Page() {
  return (
    <RequireAuthenticated>
      <ClientPage />
    </RequireAuthenticated>
  );
}
