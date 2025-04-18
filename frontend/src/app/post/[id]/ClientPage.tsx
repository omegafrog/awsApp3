"use client";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { components } from "@/lib/backend/apiV1/schema";

import { client } from "@/lib/backend/client";
import { Download } from "lucide-react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";

export default function ClinetPage({
  post,
  postGenFiles,
}: {
  post: components["schemas"]["PostWithContentDto"];
  postGenFiles: components["schemas"]["PostGenFileDto"][];
}) {
  const router = useRouter();

  const handleDelete = async () => {
    if (!confirm("정말 삭제하시겠습니까?")) {
      return;
    }

    const response = await client.DELETE("/api/v1/posts/{id}", {
      credentials: "include",
      params: {
        path: {
          id: post.id,
        },
      },
    });

    if (response.error) {
      alert(response.error.msg);
      return;
    }

    router.push("/post/list");
  };

  return (
    <div className="container p-4 mx-auto">
      <Card>
        <CardHeader>
          <CardTitle>
            <Badge variant="outline">{post.id}</Badge>
            <p className="text-4xl text-center">{post.title}</p>
          </CardTitle>
          <CardDescription className="sr-only"></CardDescription>
          <div className="flex justify-between">
            <div className="flex items-center gap-2">
              <Image
                className="w-10 h-10 rounded-full"
                src={post.authorProfileImgUrl}
                alt="프로필 이미지"
                width={80}
                height={80}
                quality={100}
              />
              <div>
                <p className="text-lg">{post.authorName}</p>
                <p className="text-sm text-gray-500">
                  {new Intl.DateTimeFormat("ko-KR", {
                    year: "numeric",
                    month: "2-digit",
                    day: "2-digit",
                    hour: "2-digit",
                    minute: "2-digit",
                    hourCycle: "h23",
                  })
                    .format(new Date(post.modifiedDate))
                    .replace(/\. /g, ". ")}
                </p>
              </div>
            </div>
            <div>
              {post.canActorHandle && (
                <div className="flex gap-4 justify-center p-4">
                  <Button>
                    <Link href={`/post/edit/${post.id}`}>수정</Link>
                  </Button>
                  <Button variant="destructive" onClick={handleDelete}>
                    삭제
                  </Button>
                </div>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent className="min-h-[calc(100vh-500px)] p-4">
          <p>{post.content}</p>
        </CardContent>
        <CardFooter>
          <div className="-mx-4">
            {postGenFiles
              .filter((file) => file.typeCode === "attachment")
              .map((file) => (
                <Button key={file.id} variant="link" asChild>
                  <a
                    href={file.downloadUrl}
                    className="flex items-center gap-2"
                  >
                    {file.fileExtTypeCode == "img" && (
                      <Image
                        src={`http://localhost:8080/gen/postGenFile/${file.typeCode}/${file.fileDateDir}/${file.fileName}`}
                        alt={file.originalFileName}
                        width={16}
                        height={16}
                        className="align-self h-[16px] w-[16px]"
                      />
                    )}
                    <Download />
                    <span>{file.originalFileName} 다운로드</span>
                  </a>
                </Button>
              ))}
          </div>
        </CardFooter>
      </Card>
    </div>
  );
}
