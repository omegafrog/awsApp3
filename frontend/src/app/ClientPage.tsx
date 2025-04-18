"use client";
import { Button } from "@/components/ui/button";
import { LoginMemberContext } from "@/stores/auth/loginMemberStore";
import { use } from "react";

export default function Page() {
  const { isLogin, loginMember } = use(LoginMemberContext);

  return (
    <>
      {!isLogin && (
        <div className="flex flex-grow justify-center items-center">
          <Button>
            <a href={`${process.env.NEXT_PUBLIC_BACKEND_HOST}/oauth2/authorization/kakao?redirectUrl=${process.env.NEXT_PUBLIC_FRONTEND_HOST}`}>
              카카오 로그인
            </a>
          </Button>
        </div>
      )}
      {isLogin && <div>{loginMember.nickname}님 환영합니다.</div>}
    </>
  );
}
