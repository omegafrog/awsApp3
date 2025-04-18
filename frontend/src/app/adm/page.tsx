import RequireAdmin from "@/components/auth/RequireAdmin";
import ClientPage from "./ClientPage";

export default function Page() {
  return (
    <RequireAdmin>
      <ClientPage />
    </RequireAdmin>
  );
}
