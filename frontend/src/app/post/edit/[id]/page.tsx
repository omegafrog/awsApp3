import { client } from "@/lib/backend/client";
import { cookies } from "next/headers";
import ClientPage from "./ClientPage";

export default async function Page({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  const response = await client.GET("/api/v1/posts/{id}", {
    params: {
      path: {
        id: parseInt(id),
      },
    },
    headers: {
      cookie: (await cookies()).toString(),
    },
  });

  if (response.error) {
    console.log(response.error.msg);
    return;
  }

  const post = response.data.data;

  return <ClientPage post={post} />;
}
