import ErrorPage from "@/components/business/ErrorPage";
import { client } from "@/lib/backend/client";
import type { Metadata } from "next";
import { cookies } from "next/headers";
import ClientPage from "./ClientPage";

export default async function Page({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const res = await fetchPost(id);

  if (res.error) {
    return <ErrorPage msg={res.error.msg} />;
  }

  const post = res.data.data;

  const postGenFilesResponse = await client.GET(
    "/api/v1/posts/{postId}/genFiles",
    {
      params: { path: { postId: post.id } },
      headers: {
        cookie: (await cookies()).toString(),
      },
    }
  );

  if (postGenFilesResponse.error) {
    return (
      <div className="flex-1 flex items-center justify-center">
        {postGenFilesResponse.error.msg}
      </div>
    );
  }

  const postGenFiles = postGenFilesResponse.data;

  return <ClientPage post={post} postGenFiles={postGenFiles} />;
}

export async function generateMetadata({
  params,
}: {
  params: Promise<{ id: string }>;
}): Promise<Metadata> {
  const { id } = await params;

  const res = await fetchPost(id);

  if (res.error) {
    return {
      title: res.error.msg,
      description: res.error.msg,
    };
  }

  const post = res.data.data;

  return {
    title: post.title,
    description: post.content,
  };
}

async function fetchPost(id: string) {
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

  return response;
}
