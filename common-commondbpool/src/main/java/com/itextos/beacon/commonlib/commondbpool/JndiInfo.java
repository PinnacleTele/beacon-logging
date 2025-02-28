package com.itextos.beacon.commonlib.commondbpool;

import java.util.Objects;

public class JndiInfo
{

    public static final JndiInfo        SYSTEM_DB        = new JndiInfo(0, "System DB");
    public static final JndiInfo CONFIGURARION_DB = new JndiInfo(1, "Configurarion DB");

    private final int            id;
    private final String         description;

    JndiInfo(
            int aId,
            String aDescription)
    {
        super();
        id          = aId;
        description = aDescription;
    }

    public int getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(
            Object obj)
    {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;
        final JndiInfo other = (JndiInfo) obj;
        return id == other.id;
    }

    @Override
    public String toString()
    {
        return "JndiInfo [id=" + id + ", description=" + description + "]";
    }

}