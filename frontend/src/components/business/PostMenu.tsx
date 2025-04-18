"use client";

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { client } from "@/lib/backend/client";
import { LoginMemberContext } from "@/stores/auth/loginMemberStore";
import { NotebookPen } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { use } from "react";
import { toast } from "sonner";

export default function HomeMenu() {
  const { isLogin } = use(LoginMemberContext);
  const router = useRouter();
  return (
    <DropdownMenu>
      <DropdownMenuTrigger>
        <div className="flex gap-2 items-center">
          <NotebookPen /> 글
        </div>
      </DropdownMenuTrigger>
      <DropdownMenuContent>
        <DropdownMenuItem asChild>
          <Link href="/post/list">글 목록</Link>
        </DropdownMenuItem>
        {isLogin && (
          <DropdownMenuItem asChild>
            <Link
              onClick={() =>
                client.POST("/api/v1/posts/temp").then((response) => {
                  if (response.error) {
                    toast.error(response.error.msg);
                  } else {
                    toast.success(response.data.msg);
                    router.replace(`/post/edit/${response.data.data.post.id}`);
                  }
                })
              }
              href={""}
            >
              글 작성
            </Link>
          </DropdownMenuItem>
        )}
        {isLogin && (
          <DropdownMenuItem asChild>
            <Link href="/post/list/me">내 글</Link>
          </DropdownMenuItem>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
