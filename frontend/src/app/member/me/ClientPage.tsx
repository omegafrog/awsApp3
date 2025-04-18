"use client";

import { LoginMemberContext } from "@/stores/auth/loginMemberStore";
import { use } from "react";
import Image from "next/image";

export default function ClinetPage() {
  const { loginMember } = use(LoginMemberContext);

  return (
    <>
      <div>내정보 페이지</div>
      <div>닉네임 : {loginMember.nickname}</div>
      <div>
        <Image
          className="w-20 h-20 rounded-full"
          src={loginMember.profileImgUrl}
          alt="프로필 이미지"
          width={80}
          height={80}
          quality={100}
        />
      </div>
    </>
  );
}
