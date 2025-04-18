"use client";

import PostList from "@/components/business/PostList";
import { components } from "@/lib/backend/apiV1/schema";

export default function ClinetPage({
  rsData,
  keywordType,
  keyword,
  pageSize,
  page,
}: {
  rsData: components["schemas"]["RsDataPageDto"];
  keywordType: "title" | "content";
  keyword: string;
  pageSize: number;
  page: number;
}) {
  return (
    <PostList
      pageDto={rsData.data}
      keywordType={keywordType}
      keyword={keyword}
      pageSize={pageSize}
      page={page}
      baseUrl="/post/list/me"
    />
  );
}
