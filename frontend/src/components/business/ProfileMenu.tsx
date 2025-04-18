"use client";

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { client } from "@/lib/backend/client";
import { LoginMemberContext } from "@/stores/auth/loginMemberStore";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { use } from "react";

export default function HomeMenu() {
  const router = useRouter();
  const { isLogin, loginMember, removeLoginMember } = use(LoginMemberContext);

  async function handleLogout(e: React.MouseEvent<HTMLAnchorElement>) {
    e.preventDefault();
    const response = await client.DELETE("/api/v1/members/logout", {
      credentials: "include",
    });

    if (response.error) {
      alert(response.error.msg);
      return;
    }

    removeLoginMember();
    router.replace("/");
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger>
        {isLogin && (
          <DropdownMenuLabel>
            <div className="flex gap-2 items-center">
              <div>
                <Image
                  className="w-10 h-10 rounded-full"
                  src={loginMember.profileImgUrl}
                  alt="프로필 이미지"
                  width={80}
                  height={80}
                  quality={100}
                />
              </div>
            </div>
          </DropdownMenuLabel>
        )}
      </DropdownMenuTrigger>
      <DropdownMenuContent>
        {isLogin && (
          <DropdownMenuLabel>
            <div className="text-lg">{loginMember.nickname}</div>
          </DropdownMenuLabel>
        )}
        {isLogin && (
          <DropdownMenuItem asChild>
            <Link href="" onClick={handleLogout}>
              로그아웃
            </Link>
          </DropdownMenuItem>
        )}
        {isLogin && (
          <DropdownMenuItem asChild>
            <Link href="/member/me">내정보</Link>
          </DropdownMenuItem>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
