"use client";

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { LoginMemberContext } from "@/stores/auth/loginMemberStore";
import { House } from "lucide-react";
import Link from "next/link";
import { use } from "react";

export default function HomeMenu() {
  const { isLogin } = use(LoginMemberContext);
  return (
    <DropdownMenu>
      <DropdownMenuTrigger>
        <div className="flex gap-2 items-center">
          <House />
          Home
        </div>
      </DropdownMenuTrigger>
      <DropdownMenuContent>
        <DropdownMenuItem asChild>
          <Link href="/">메인</Link>
        </DropdownMenuItem>
        <DropdownMenuItem asChild>
          <Link href="/about">소개</Link>
        </DropdownMenuItem>
        {!isLogin && (
          <DropdownMenuItem asChild>
            <Link href="/adm/member/login">관리자 로그인</Link>
          </DropdownMenuItem>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
