const Roles: Record<string, string> = {
    "ROLE_ADMIN": "Администратор",
    "ROLE_COORDINATOR": "Координатор",
    "ROLE_EXECUTOR": "Исполнитель"
}

type Credentials = {
    username: string,
    password: string
}

export { Roles }
export type { Credentials }
