"use client";

import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { components } from "@/lib/backend/apiV1/schema";
import { client, clientWithNoHeaders } from "@/lib/backend/client";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { z } from "zod";

const editFormSchema = z.object({
  title: z.string().min(1, "제목을 입력해주세요."),
  content: z.string().min(1, "내용을 입력해주세요."),
  published: z.boolean().optional(),
  listed: z.boolean().optional(),
  attachment_0: z.array(z.instanceof(File)).optional(),
});

type WriteInputs = z.infer<typeof editFormSchema>;

export default function ClinetPage({
  post,
}: {
  post: components["schemas"]["PostWithContentDto"];
}) {
  const router = useRouter();
  const form = useForm<WriteInputs>({
    resolver: zodResolver(editFormSchema),
    defaultValues: {
      title: post.title,
      content: post.content,
      published: post.published,
      listed: post.listed,
    },
  });

  async function write(data: WriteInputs) {
    const title = data.title;
    const content = data.content;
    const published = data.published;
    const listed = data.listed;

    const response = await client.PUT("/api/v1/posts/{id}", {
      params: {
        path: { id: post.id },
      },
      body: {
        title,
        content,
        published: published ?? false,
        listed: listed ?? false,
      },
      credentials: "include",
    });

    if (response.error) {
      alert(response.error.msg);
      return;
    }

    // 파일 업로드 처리
    if (data.attachment_0) {
      const formData = new FormData();
      for (const file of [...data.attachment_0].reverse())
        formData.append("files", file);

      const uploadResponse = await clientWithNoHeaders.POST(
        "/api/v1/posts/{postId}/genFiles/{typeCode}",
        {
          params: {
            path: {
              postId: post.id,
              typeCode: "attachment",
            },
          },
          body: formData as any, // eslint-disable-line @typescript-eslint/no-explicit-any
          credentials: "include",
        }
      );

      if (uploadResponse.error) {
        alert(uploadResponse.error.msg);
        return;
      }
    }

    // 목록으로 이동, 내가 방금 작성한 글 상세 페이지 이동 => 리액트 방식의 페이지 이동
    router.push(`/post/${post.id}`);
  }

  return (
    <div className="container p-4 mx-auto">
      <h1 className="text-2xl font-bold text-center">글 수정</h1>
      <hr />
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(write)}
          className="flex flex-col gap-3 py-4"
        >
          <div className="flex items-center gap-3">
            <label>공개 여부 : </label>
            <FormField
              control={form.control}
              name="published"
              render={({ field }) => (
                <Checkbox
                  checked={field.value}
                  onCheckedChange={field.onChange}
                />
              )}
            />
          </div>
          <div className="flex items-center gap-3">
            <label>검색 여부 : </label>
            <FormField
              control={form.control}
              name="listed"
              render={({ field }) => (
                <Checkbox
                  checked={field.value}
                  onCheckedChange={field.onChange}
                />
              )}
            />
          </div>
          <FormField
            control={form.control}
            name="title"
            render={({ field }) => (
              <FormItem>
                <FormLabel>제목</FormLabel>
                <FormControl>
                  <Input
                    {...field}
                    type="text"
                    placeholder="제목 입력"
                    className="border-2 border-black"
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="attachment_0"
            render={({ field: { onChange, ...field } }) => (
              <FormItem>
                <FormLabel>첨부파일</FormLabel>
                <FormControl>
                  <Input
                    type="file"
                    multiple
                    onChange={(e) => {
                      const files = Array.from(e.target.files || []);
                      onChange(files);
                    }}
                    {...field}
                    value={undefined}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="content"
            render={({ field }) => (
              <FormItem>
                <FormLabel>내용</FormLabel>
                <FormControl>
                  <Textarea
                    {...field}
                    placeholder="내용 입력"
                    className="h-[calc(100dvh-460px)] min-h-[300px]"
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <Button type="submit">수정</Button>
        </form>
      </Form>
    </div>
  );
}
